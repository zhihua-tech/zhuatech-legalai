/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.legalai.workbench;

import static cn.zhuatech.legalai.workbench.WorkbenchInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "before", "after");
    Map<String, String> before = new LinkedHashMap<>(), after = new LinkedHashMap<>();
    for (String side : List.of("before", "after")) {
      Map<String, String> target = side.equals("before") ? before : after;
      for (var c : rows(r, side, 0, 200)) {
        keys(c, "id", "text");
        String id = id(c, "id");
        if (target.putIfAbsent(id, text(c, "text", 8000)) != null)
          throw new IllegalArgumentException("条款编号重复");
      }
    }
    if (before.isEmpty() && after.isEmpty()) throw new IllegalArgumentException("至少提供一条条款");
    Set<String> ids = new TreeSet<>(before.keySet());
    ids.addAll(after.keySet());
    List<Map<String, Object>> out = new ArrayList<>();
    int changed = 0;
    for (String id : ids) {
      String a = before.get(id), b = after.get(id);
      String status =
          a == null
              ? "ADDED"
              : b == null
                  ? "REMOVED"
                  : a.equals(b)
                      ? "UNCHANGED"
                      : a.replaceAll("\\s+", " ").strip().equals(b.replaceAll("\\s+", " ").strip())
                          ? "FORMAT_ONLY"
                          : "MODIFIED";
      if (!status.equals("UNCHANGED") && !status.equals("FORMAT_ONLY")) changed++;
      out.add(
          Map.of(
              "id",
              id,
              "status",
              status,
              "before",
              a == null ? "" : a,
              "after",
              b == null ? "" : b,
              "beforeDigest",
              a == null ? "" : digest(a),
              "afterDigest",
              b == null ? "" : digest(b)));
    }
    return Map.of("substantiveChanges", changed, "clauses", out, "legalOpinion", false);
  }
}
