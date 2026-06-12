package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.service.IHotQuestionService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 热点问题服务 — 内置 20 条热点问答
 */
@Service
public class HotQuestionServiceImpl implements IHotQuestionService {

    private static final List<Map<String, String>> HOT_QUESTIONS = new ArrayList<>();

    static {
        add("有什么热销商品推荐？", "本月热销前三：① iPhone 15 Pro Max（销量 350+，好评 98%）② 三只松鼠每日坚果（销量 2100+，好评 99%）③ MacBook Pro 14英寸（销量 180+，好评 99%）。点击商品卡片可查看详情～");
        add("怎么下单购买？", "1. 将商品加入购物车 → 2. 去购物车勾选 → 3. 点击「结算」→ 4. 选择收货地址和支付方式 → 5. 提交订单。支付后等待发货即可～");
        add("支持哪些支付方式？", "目前支持两种支付方式：① 模拟支付（MOCK_PAY，金额扣减演示）② 余额支付（BALANCE，从账户余额扣减）。");
        add("怎么查看物流信息？", "进入「我的订单」，点击对应订单即可查看物流详情。配送中会显示配送员姓名和联系电话，可直接联系配送员～");
        add("怎样申请退款？", "在「我的订单」中找到目标订单，若状态为「待支付」或「待发货」，可直接取消并退款；若已配送中，需确认收货后申请售后。");
        add("忘记密码怎么办？", "在登录页面点击「忘记密码」，按提示输入注册手机号，接收验证码后即可重置密码。");
        add("商品是正品吗？", "悦选商城所有商品均为品牌直供或授权经销商供货，100% 正品保障。如有疑问可联系客服查验授权证明～");
        add("多久能发货？", "一般情况下，现货商品会在 24 小时内发货。预售商品以商品页面标注时间为准。发货后 1-3 天送达（偏远地区除外）。");
        add("运费怎么算？", "全场满 99 元包邮，不满 99 元收取 3 元基础运费。部分大件商品（如电脑、家电）按实际重量计费，以结算页面为准。");
        add("怎么联系客服？", "您可以直接在 AI 对话框中输入问题，AI 助手 7×24 小时在线。如需人工客服，请拨打 400-888-0000（工作日 9:00-18:00）。");
        add("如何修改收货地址？", "下单前：在「个人中心-收货地址」中修改或新增地址。下单后：如订单未发货，可联系客服修改；已发货则无法修改地址。");
        add("支持 7 天无理由退货吗？", "支持！自签收之日起 7 天内，商品完好不影响二次销售的情况下，可申请无理由退货。部分商品（内衣裤、食品等）除外。");
        add("优惠券怎么使用？", "在结算页面会自动匹配可用优惠券。目前平台优惠：满 199 减 20、满 499 减 50。部分商品可叠加品牌券。");
        add("如何查看我的积分？", "积分功能即将上线，敬请期待！目前您可以在「个人中心」查看订单数量和消费记录。");
        add("如何评价已购商品？", "订单完成后，在「我的订单」中点击「去评价」，可对商品进行评分（1-5 星）和文字评价，也可上传图片晒单。");
        add("MacBook Pro 和 ThinkPad 怎么选？", "MacBook Pro 14英寸 M3 Pro（¥14,999）：适合设计、视频剪辑和开发，macOS 生态优异。ThinkPad X1 Carbon（¥12,999）：适合商务办公和出差，1.12kg 超轻机身，军规级耐用。您主要用它做什么？");
        add("iPhone 和华为怎么选？", "iPhone 15 Pro Max（¥9,999）：A17 Pro 芯片，适合游戏和创作，iOS 生态流畅。华为 Mate 60 Pro（¥7,999）：支持卫星通话，昆仑玻璃耐摔，鸿蒙多设备协同。看您目前用什么系统，需要卫星通话功能吗？");
        add("有适合送礼的商品吗？", "推荐：① 资生堂红腰子精华 75ml（¥589）- 送女性长辈/朋友的护肤礼 ② 武夷山大红袍礼盒 500g（¥298）- 送长辈 ③ 三只松鼠每日坚果礼盒（¥68）- 送同事/亲友 ④ 无印良品香薰机（¥248）- 暖居礼物");
        add("账户余额怎么充值？", "目前余额充值功能正在开发中。您可以直接使用模拟支付完成购买，无需充值。未来余额功能上线后可享受更多优惠～");
        add("如何注册账号？", "在登录页面点击「还没有账号？立即注册」，填写用户名、手机号、密码即可完成注册。注册后即可浏览商品、加入购物车和下单。");
    }

    private static void add(String question, String answer) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("question", question);
        item.put("answer", answer);
        HOT_QUESTIONS.add(item);
    }

    @Override
    public List<Map<String, String>> getHotQuestions(int limit) {
        return HOT_QUESTIONS.subList(0, Math.min(limit, HOT_QUESTIONS.size()));
    }

    @Override
    public List<Map<String, String>> getAllHotQuestions() {
        return new ArrayList<>(HOT_QUESTIONS);
    }
}
