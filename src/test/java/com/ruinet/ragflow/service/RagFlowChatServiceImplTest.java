package com.ruinet.ragflow.service;

import com.ruinet.ragflow.exception.ServiceException;
import com.ruinet.ragflow.pojo.vo.RagFlowChatAssistantCreateReqVO;
import com.ruinet.ragflow.pojo.vo.RagFlowChatAssistantRespVO;
import com.ruinet.ragflow.pojo.vo.RagFlowChatReqVO;
import com.ruinet.ragflow.pojo.vo.RagFlowChatRespVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

/**
 * RagFlowChatServiceImpl 核心集成逻辑的单元测试。
 *
 * @author 中锐网络
 */
@SpringBootTest
public class RagFlowChatServiceImplTest {

    @Autowired
    private RagFlowChatService ragFlowChatService;

    /**
     * 测试当请求体为空时，是否按规约提前抛出 ServiceException。
     */
    @Test
    public void testSendChat_NullRequest_ThrowsException() {
        ServiceException exception = Assertions.assertThrows(ServiceException.class, () -> {
            ragFlowChatService.sendChat(null);
        });
        Assertions.assertTrue(exception.getMessage().contains("请求体不能为空"));
    }

    /**
     * 测试提问文本为空时，是否按规约提前抛出 ServiceException。
     */
    @Test
    public void testSendChat_EmptyMessage_ThrowsException() {
        RagFlowChatReqVO reqVO = new RagFlowChatReqVO();
        reqVO.setMessage("");
        
        ServiceException exception = Assertions.assertThrows(ServiceException.class, () -> {
            ragFlowChatService.sendChat(reqVO);
        });
        Assertions.assertTrue(exception.getMessage().contains("提问内容不能为空"));
    }

    /**
     * 真实联调集成测试（当您的 application.yml 已填入真实的 RAGFlow BaseUrl、ApiKey 以及 ChatId 时，移去 @Disabled 运行）。
     */
    @Test
    @Disabled("真实联网调试时手动启用")
    public void testSendChat_Success() {
        RagFlowChatReqVO reqVO = new RagFlowChatReqVO();
        reqVO.setMessage("你好！请根据关联的知识库向我介绍你们的产品特征");
        
        RagFlowChatRespVO respVO = ragFlowChatService.sendChat(reqVO);
        
        Assertions.assertNotNull(respVO);
        Assertions.assertNotNull(respVO.getAnswer());
        System.out.println("RAGFlow 回复内容：");
        System.out.println(respVO.getAnswer());
    }

    /**
     * 测试创建聊天助手时入参为空，是否按规约提前抛出 ServiceException。
     */
    @Test
    public void testCreateChatAssistant_NullRequest_ThrowsException() {
        ServiceException exception = Assertions.assertThrows(ServiceException.class, () -> {
            ragFlowChatService.createChatAssistant(null);
        });
        Assertions.assertTrue(exception.getMessage().contains("请求入参不能为空"));
    }

    /**
     * 真实联调集成测试创建专属知识库聊天助手（真实联网调试时手动启用）。
     */
    @Test
    @Disabled("真实联网调试时手动启用")
    public void testCreateChatAssistant_Success() {
        RagFlowChatAssistantCreateReqVO reqVO = new RagFlowChatAssistantCreateReqVO();
        reqVO.setName("单元测试新建聊天助手");
        reqVO.setLlmId("qwen-plus@Tongyi-Qianwen");
        // 注意：因为 "0b2cbc8c877f11ef89070242ac120005" 为演示样例ID，在没有真实拥有它的 API 密钥下会触发
        // "You don't own the dataset" 的 RAGFlow 业务逻辑报错。
        // 如果进行真实空关联测试，请设为空列表：Collections.emptyList()。
        // 若要绑定真实知识库，请在此处替换为您在 RAGFlow 控制台中实际拥有的真实知识库 ID 列表。
        reqVO.setDatasetIds(Collections.emptyList());
        
        RagFlowChatAssistantCreateReqVO.LlmSettingVO llmSetting = new RagFlowChatAssistantCreateReqVO.LlmSettingVO();
        llmSetting.setTemperature(0.1);
        llmSetting.setTopP(0.3);
        reqVO.setLlmSetting(llmSetting);
        
        RagFlowChatAssistantCreateReqVO.PromptConfigVO promptConfig = new RagFlowChatAssistantCreateReqVO.PromptConfigVO();
        promptConfig.setSystem("You are an intelligent assistant...");
        promptConfig.setPrologue("您好！我是您的智能搜索助理。");
        promptConfig.setQuote(true);
        reqVO.setPromptConfig(promptConfig);

        RagFlowChatAssistantRespVO respVO = ragFlowChatService.createChatAssistant(reqVO);

        Assertions.assertNotNull(respVO);
        Assertions.assertNotNull(respVO.getId());
        Assertions.assertEquals("单元测试新建聊天助手", respVO.getName());
        System.out.println("成功创建聊天助手，ID 为：" + respVO.getId());
    }
}
