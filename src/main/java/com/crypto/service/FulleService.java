package com.crypto.service;

import com.crypto.entity.FulleCompany;
import com.crypto.entity.FulleShareholder;
import com.crypto.mapper.FulleCompanyMapper;
import com.crypto.mapper.FulleShareholderMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class FulleService {

    private final FulleCompanyMapper companyMapper;
    private final FulleShareholderMapper shareholderMapper;

    public FulleService(FulleCompanyMapper companyMapper, FulleShareholderMapper shareholderMapper) {
        this.companyMapper = companyMapper;
        this.shareholderMapper = shareholderMapper;
    }

    // ==================== 公司管理 ====================

    public List<FulleCompany> getAllCompanies() {
        return companyMapper.findAll();
    }

    public FulleCompany getCompanyById(Long id) {
        return companyMapper.findById(id);
    }

    public FulleCompany getRootCompany() {
        return companyMapper.findByRoot();
    }

    public int createCompany(FulleCompany c) {
        if (c.getLevel() == null) c.setLevel(0);
        return companyMapper.insert(c);
    }

    public int updateCompany(FulleCompany c) {
        return companyMapper.update(c);
    }

    public int deleteCompany(Long id) {
        return companyMapper.delete(id);
    }

    // ==================== 股东管理 ====================

    public List<FulleShareholder> getShareholdersByCompany(Long companyId) {
        return shareholderMapper.findByCompanyId(companyId);
    }

    public List<FulleShareholder> getShareholdersByName(String name) {
        return shareholderMapper.findByHolderNameLike(name);
    }

    public List<FulleShareholder> getAllShareholders() {
        return shareholderMapper.findAll();
    }

    public int createShareholder(FulleShareholder s) {
        return shareholderMapper.insert(s);
    }

    public int updateShareholder(FulleShareholder s) {
        return shareholderMapper.update(s);
    }

    public int deleteShareholder(Long id) {
        return shareholderMapper.delete(id);
    }

    // ==================== 股权穿透计算核心 ====================

    /**
     * 股权穿透结果
     */
    public static class EquityResult {
        public String holderName;          // 自然人姓名
        public String platformName;        // 所属平台
        public Long platformId;            // 所属平台ID
        public BigDecimal directRatio;     // 在所属平台的直接持股比例(%)
        public String pathChain;           // 完整路径链
        public BigDecimal pathRatio;       // 该路径折算到顶层的占比(%)
        public BigDecimal topRatio;        // 该人在顶层的最终总占比(%)（多路径汇总）
        public BigDecimal currentValue;    // 股权当前价值（亿元）topRatio/100 * 市值

        public EquityResult() {}
    }

    /**
     * 加载股权图数据
     */
    private static class EquityGraph {
        Map<Long, FulleCompany> companies = new LinkedHashMap<>();
        // companyId -> List<shareholder>  （companyId 是"被投资的平台"，持股人列表）
        Map<Long, List<FulleShareholder>> holdersMap = new LinkedHashMap<>();

        EquityGraph(List<FulleCompany> companies, List<FulleShareholder> holders) {
            for (FulleCompany c : companies) {
                this.companies.put(c.getId(), c);
            }
            for (FulleShareholder s : holders) {
                this.holdersMap.computeIfAbsent(s.getCompanyId(), k -> new ArrayList<>()).add(s);
            }
        }
    }

    /**
     * 核心：计算所有自然人到顶层（富勒）的股权穿透
     * 策略：只处理直接在某个平台出现的自然人（PERSON），
     * 然后沿 linked_company_id 向上追溯找到顶层。
     * COMPANY子级的自然人通过递归展开子级公司时自然覆盖。
     */
    public List<EquityResult> computeAllEquity(BigDecimal marketValue) {
        List<FulleCompany> allCompanies = companyMapper.findAll();
        List<FulleShareholder> allHolders = shareholderMapper.findAll();
        EquityGraph graph = new EquityGraph(allCompanies, allHolders);

        FulleCompany root = getRootCompany();
        if (root == null) return Collections.emptyList();

        Map<String, BigDecimal> holderTotalMap = new LinkedHashMap<>();
        List<EquityResult> allResults = new ArrayList<>();
        // 去重：holderName + platformId + chain
        Set<String> dedup = new HashSet<>();

        // 只遍历自然人持股记录（PERSON类型），不管COMPANY类型
        for (FulleShareholder sh : allHolders) {
            if (!"PERSON".equals(sh.getHolderType())) continue;

            Long platformId = sh.getCompanyId();
            FulleCompany platform = graph.companies.get(platformId);
            if (platform == null) continue;

            String pName = getShortName(platform);
            BigDecimal directRatio = sh.getShareRatio();

            if (platformId.equals(root.getId())) {
                // 直接在顶层公司持股
                String chain = sh.getHolderName() + "→" + pName + "(" + fmt(directRatio) + "%)";
                String key = sh.getHolderName() + "|" + platformId + "|" + chain;
                if (!dedup.add(key)) continue;

                EquityResult er = new EquityResult();
                er.holderName = sh.getHolderName();
                er.platformName = pName;
                er.platformId = platformId;
                er.directRatio = directRatio;
                er.pathChain = chain;
                er.pathRatio = directRatio.setScale(6, RoundingMode.HALF_UP);
                allResults.add(er);
                holderTotalMap.merge(sh.getHolderName(),
                    directRatio.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
            } else {
                // 从该平台向上找到顶层的所有路径
                List<List<EquityStep>> allPaths = findPathsToRoot(platformId, root.getId(), graph);

                for (List<EquityStep> path : allPaths) {
                    StringBuilder chain = new StringBuilder(sh.getHolderName());
                    chain.append("→").append(pName).append("(").append(fmt(directRatio)).append("%)");

                    BigDecimal cumulative = directRatio;
                    for (EquityStep step : path) {
                        cumulative = cumulative
                            .multiply(step.parentRatio)
                            .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                        chain.append("→").append(getShortName(step.parentCompany))
                            .append("(").append(fmt(step.parentRatio)).append("%)");
                    }

                    String key = sh.getHolderName() + "|" + platformId + "|" + chain.toString();
                    if (!dedup.add(key)) continue;

                    EquityResult er = new EquityResult();
                    er.holderName = sh.getHolderName();
                    er.platformName = pName;
                    er.platformId = platformId;
                    er.directRatio = directRatio;
                    er.pathChain = chain.toString();
                    er.pathRatio = cumulative.setScale(6, RoundingMode.HALF_UP);
                    allResults.add(er);
                    holderTotalMap.merge(sh.getHolderName(),
                        cumulative.setScale(6, RoundingMode.HALF_UP), BigDecimal::add);
                }
            }
        }

        // 回填 topRatio
        for (EquityResult er : allResults) {
            er.topRatio = holderTotalMap.getOrDefault(er.holderName, er.pathRatio)
                .setScale(6, RoundingMode.HALF_UP);
        }

        // 计算股权当前价值（市值 * pathRatio / 100）
        if (marketValue != null && marketValue.compareTo(BigDecimal.ZERO) > 0) {
            for (EquityResult er : allResults) {
                er.currentValue = marketValue
                    .multiply(er.pathRatio)
                    .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            }
        }

        // 按topRatio降序，同人按pathRatio降序
        allResults.sort((a, b) -> {
            int cmp = b.topRatio.compareTo(a.topRatio);
            if (cmp != 0) return cmp;
            return b.pathRatio.compareTo(a.pathRatio);
        });

        return allResults;
    }

    /**
     * 从某个公司向上找到顶层公司的所有路径
     * 通过查找"哪个公司在股东表里持有该公司"来向上追溯
     */
    private List<List<EquityStep>> findPathsToRoot(Long companyId, Long rootId, EquityGraph graph) {
        List<List<EquityStep>> results = new ArrayList<>();
        findPathsRecursive(companyId, rootId, graph, new ArrayList<>(), new HashSet<>(), results);
        return results;
    }

    private static class EquityStep {
        FulleCompany parentCompany;
        BigDecimal parentRatio; // parentCompany 持有当前公司的比例

        EquityStep(FulleCompany parentCompany, BigDecimal parentRatio) {
            this.parentCompany = parentCompany;
            this.parentRatio = parentRatio;
        }
    }

    private void findPathsRecursive(Long currentCompanyId, Long rootId, EquityGraph graph,
                                    List<EquityStep> currentPath, Set<Long> visited,
                                    List<List<EquityStep>> results) {
        if (currentCompanyId.equals(rootId) || visited.contains(currentCompanyId)) {
            if (!currentPath.isEmpty()) {
                results.add(new ArrayList<>(currentPath));
            }
            return;
        }
        visited.add(currentCompanyId);

        // 查找谁持有 currentCompanyId
        // 遍历所有持股记录
        for (Map.Entry<Long, List<FulleShareholder>> ent : graph.holdersMap.entrySet()) {
            Long parentId = ent.getKey();
            List<FulleShareholder> parentHolders = ent.getValue();

            for (FulleShareholder h : parentHolders) {
                if (h.getLinkedCompanyId() != null && h.getLinkedCompanyId().equals(currentCompanyId)) {
                    FulleCompany parentCompany = graph.companies.get(parentId);
                    if (parentCompany == null) continue;

                    currentPath.add(new EquityStep(parentCompany, h.getShareRatio()));
                    findPathsRecursive(parentId, rootId, graph, currentPath, visited, results);
                    currentPath.remove(currentPath.size() - 1);
                }
            }
        }
        visited.remove(currentCompanyId);
    }

    /**
     * 按人查询股权穿透
     */
    public List<EquityResult> computeEquityByName(String name, BigDecimal marketValue) {
        List<EquityResult> all = computeAllEquity(marketValue);
        List<EquityResult> filtered = new ArrayList<>();
        for (EquityResult r : all) {
            if (r.holderName.contains(name)) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    /**
     * 按子公司查询股权穿透
     */
    public List<EquityResult> computeEquityByCompany(Long companyId, BigDecimal marketValue) {
        FulleCompany target = companyMapper.findById(companyId);
        if (target == null) return Collections.emptyList();

        List<EquityResult> all = computeAllEquity(marketValue);
        FulleCompany root = getRootCompany();

        List<EquityResult> filtered = new ArrayList<>();
        Map<String, BigDecimal> holderTotalByCompany = new LinkedHashMap<>();

        for (EquityResult r : all) {
            // 检查路径链中是否包含目标公司
            if (r.pathChain != null && r.pathChain.contains(getShortName(target))) {
                // 提取该人在目标公司层面的折算占比
                BigDecimal ratioAtCompany = extractRatioAtCompany(r, target);
                if (ratioAtCompany == null) continue;

                r.topRatio = ratioAtCompany; // 在该公司的总占比
                filtered.add(r);
                holderTotalByCompany.merge(r.holderName, ratioAtCompany, BigDecimal::add);
            }
        }

        // 直接查目标公司的股东（包括通过子公司间接持股的）
        List<FulleShareholder> directHolders = shareholderMapper.findByCompanyId(companyId);
        if (directHolders != null) {
            Set<String> existingNames = new HashSet<>();
            for (EquityResult er : filtered) existingNames.add(er.holderName);

            for (FulleShareholder sh : directHolders) {
                if ("PERSON".equals(sh.getHolderType()) && !existingNames.contains(sh.getHolderName())) {
                    String sName = getShortName(target);
                    EquityResult er = new EquityResult();
                    er.holderName = sh.getHolderName();
                    er.platformName = sName;
                    er.platformId = companyId;
                    er.directRatio = sh.getShareRatio();
                    er.pathChain = sh.getHolderName() + "→" + sName + "(" + fmt(sh.getShareRatio()) + "%)";
                    er.pathRatio = sh.getShareRatio();
                    er.topRatio = sh.getShareRatio();
                    filtered.add(er);
                    holderTotalByCompany.merge(sh.getHolderName(), sh.getShareRatio(), BigDecimal::add);
                }
            }
        }

        // 回填 topRatio
        for (EquityResult er : filtered) {
            er.topRatio = holderTotalByCompany.getOrDefault(er.holderName, er.pathRatio)
                .setScale(6, RoundingMode.HALF_UP);
        }

        filtered.sort((a, b) -> b.topRatio.compareTo(a.topRatio));
        return filtered;
    }

    /**
     * 从路径链中提取在某个公司层面的折算占比
     */
    private BigDecimal extractRatioAtCompany(EquityResult r, FulleCompany target) {
        if (r.pathChain == null) return null;
        String targetName = getShortName(target);
        // 路径链格式: 张三→勒宜(3.46%)→勒坤(14.6%)→富勒(27.54%)
        // 找到目标公司位置，计算到那一步的累计折算
        String[] parts = r.pathChain.split("→");
        BigDecimal cumulative = null;
        boolean found = false;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            // 提取比例
            int openIdx = part.indexOf('(');
            if (openIdx < 0) continue;
            int closeIdx = part.indexOf(')');
            if (closeIdx < 0) continue;
            String ratioStr = part.substring(openIdx + 1, closeIdx).replace("%", "");
            BigDecimal ratio = new BigDecimal(ratioStr);

            if (cumulative == null) {
                cumulative = ratio;
            } else {
                cumulative = cumulative.multiply(ratio)
                    .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
            }

            String entityName = part.substring(0, openIdx);
            if (entityName.equals(targetName)) {
                found = true;
                break;
            }
        }
        return found && cumulative != null ? cumulative.setScale(6, RoundingMode.HALF_UP) : null;
    }

    private String getShortName(FulleCompany c) {
        return c.getShortName() != null ? c.getShortName() : c.getCompanyName();
    }

    private String fmt(BigDecimal ratio) {
        return ratio.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
