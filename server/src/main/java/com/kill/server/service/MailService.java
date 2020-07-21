package com.kill.server.service;

import com.kill.model.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@EnableAsync
public class MailService {

    private static final Logger log= LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender MailSender;

    @Autowired
    private Environment env;

    @Async
    public void sendHTMLMail(final MailDto mailDto) throws MessagingException {
        try {
            MimeMessage mimeMessage = MailSender.createMimeMessage();
            MimeMessageHelper messageHelper=new MimeMessageHelper(mimeMessage,true,"UTF-8");
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setTo(mailDto.getTos());
            messageHelper.setSubject(mailDto.getSubject());
            messageHelper.setText(mailDto.getContent(),true);
            MailSender.send(mimeMessage);
            log.info("发送邮件成功");
        }catch (Exception e){
            log.error("发送邮件发生异常：",e.fillInStackTrace());
        }
    }

}
