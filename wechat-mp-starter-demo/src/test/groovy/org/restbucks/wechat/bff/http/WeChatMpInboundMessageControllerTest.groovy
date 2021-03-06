package org.restbucks.wechat.bff.http

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.mockito.BDDMockito.given
import static org.mockito.Matchers.refEq
import static org.mockito.Mockito.verify
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class WeChatMpInboundMessageControllerTest extends AbstractWebMvcTest {

    @Test
    void returns_echostr_to_get_authenticated_by_wechat_server() {

        def timestamp = "timestamp"
        def nonce = "nonce"

        given(wxMpService.checkSignature(timestamp, nonce, "good")).willReturn(true)

        // @formatter:off
        this.mockMvc.perform(
                    get("/wechat/mp/webhooks/messaging")
                    .param("signature", "good")
                    .param("echostr", "echostr")
                    .param("timestamp", timestamp)
                    .param("nonce", nonce)
                )
                .andDo(print())
	            .andExpect(status().isOk())
                .andExpect(content().string("echostr"))
        // @formatter:on
    }

    @Test
    void returns_error_other_than_echostr_to_refuse_authentication_by_wechat_server() {

        def timestamp = "timestamp"
        def nonce = "nonce"

        given(wxMpService.checkSignature(timestamp, nonce, "bad")).willReturn(false)

        // @formatter:off
        this.mockMvc.perform(
                    get("/wechat/mp/webhooks/messaging")
                    .param("signature", "bad")
                    .param("echostr", "echostr")
                    .param("timestamp", timestamp)
                    .param("nonce", nonce)
                )
                .andDo(print())
	            .andExpect(status().isOk())
                .andExpect(content().string(not(equalTo("echostr"))))
        // @formatter:on
    }

    @Test
    void when_receives_qrcode_scanned_event() {

        String payload = """
            <xml>
                <ToUserName><![CDATA[toUser]]></ToUserName>
                <FromUserName><![CDATA[FromUser]]></FromUserName>
                <CreateTime>123456789</CreateTime>
                <MsgType><![CDATA[event]]></MsgType>
                <Event><![CDATA[subscribe]]></Event>
                <EventKey><![CDATA[qrscene_123123]]></EventKey>
                <Ticket><![CDATA[TICKET]]></Ticket>
            </xml>
        """
        // @formatter:off
        this.mockMvc.perform(
                    post("/wechat/mp/webhooks/messaging")
                    .content(payload)
                )
                .andDo(print())
	            .andExpect(status().isOk())
        // @formatter:on

        verify(wxMpMessageRouter).route(refEq(WxMpXmlMessage.fromXml(payload)))
    }


}
