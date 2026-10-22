# 合同条款版本差异

知华科技（上海如静知华信息科技有限公司） · https://www.zhuatech.cn/

## 本次实现

按条款编号对比新增、删除、实质变更和仅排版变更，提供修改前后文本及摘要；不作法律有效性判断。

管理端与业务工作台登录后右下角「业务计算工具」进入。输入是可编辑的 JSON；提供合成示例、校验、加载反馈、错误提示、前100行预览和完整 JSON 导出。未登录时不显示入口；演示令牌不能调用。

## 接口 / API

`POST /api/workbench/clause-revision-diff`

- `Content-Type: application/json`；使用现有登录返回的 Bearer JWT，不新增默认账号。
- 成功200：`{success:true,message,data}`；无有效身份401；参数错误400。现有安全过滤器继续生效。
- 所有已认证账号均可对自行提交的数据运算；不查询业务库，因此不授予新的业务记录读写权限。调用方必须自行取得输入数据的处理权限。
- 严格拒绝未知字段、重复业务编号、非法日期及越界数值。最大深度12、节点10000、单字段64000字符、累计128000字符、通用数组最多500项；各业务还有限制，见 `WorkbenchInput` 和 `AiWorkbenchService`。
- 应在生产网关配置128 KiB请求体与速率限制；当前业务校验发生在JSON解析之后，不把它当作传输层防护。

### 请求字段

| 字段 | 类型 |
| --- | --- |
| `before` | array |
| `after` | array |

所有字段必填，空数组仅在示例/服务允许的场景使用。日期采用 ISO-8601；字段单位和输出算法见服务代码及以下示例。

```json
{
  "before": [
    {
      "id": "C1",
      "text": "付款期限30天"
    },
    {
      "id": "C2",
      "text": "旧条款"
    }
  ],
  "after": [
    {
      "id": "C1",
      "text": "付款期限60天"
    },
    {
      "id": "C3",
      "text": "新增条款"
    }
  ]
}
```

### 调用示例

```sh
# 将上方请求保存为 request.json；TOKEN 为自己登录获得的短期令牌。
curl --fail-with-body -X POST "$API_BASE/api/workbench/clause-revision-diff" \\
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \\
  --data-binary @request.json
```

## 计算口径与边界

前后版本各最多200条条款，单条最长8000字符，另受累计128000字符限制。先精确比较，再把连续空白规范为一个空格并去首尾空白，判断是否仅排版变化；不会去掉单词间空格。保留完整前后文本和SHA-256摘要，但摘要不证明签署或法律效力。

## 复核与限制

这是已经实现并经过单元及控制器测试的本地算法，不是待对接占位接口。结果需要业务人员复核。不会调用外部模型、不修改或持久化业务记录，不自动审批、执行工具、通知或改变生产状态。输入与结果只在当前页面内存中，刷新/退出登录后不保留；需要时手动导出。不要输入未经授权的生产敏感数据。

新增 `AiWorkbenchServiceTest` 的14项测试覆盖业务结果、两项领域边界、参数边界、确定性/不修改输入、空请求、未知字段、长度/嵌套限制及HTTP认证、成功与错误；另有 `AiWorkbenchSecurityIntegrationTest` 的4项测试经过实际安全过滤器验证有效JWT、匿名、无效令牌和参数错误。集成环境使用H2，测试令牌由应用测试上下文签发；不添加运行时账号或密钥。运行 `cd backend && mvn test` 和 `cd frontend && npm run build`。真实MySQL全流程、容量与真实模型验收不在这些测试结论之内。

## English summary

**Clause revision comparison.** Compares clause revisions by ID, identifying additions, removals, substantive changes and whitespace-run-only changes while retaining word boundaries. No legal opinion is generated. Use the sample above with the documented POST endpoint. Validation errors are explicit; no model invocation, database update, automated approval or tool execution is performed. JSON results can be exported. Existing licensing terms remain unchanged.

Commercial licensing or customization: [han@zhuatech.cn](mailto:han@zhuatech.cn), [jack@zhuatech.cn](mailto:jack@zhuatech.cn), [WhatsApp +86 17521234993](https://wa.me/8617521234993).

商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。中文README原有两个微信二维码继续保留。
