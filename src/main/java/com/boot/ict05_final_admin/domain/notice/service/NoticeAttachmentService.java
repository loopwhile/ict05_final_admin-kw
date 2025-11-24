package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeAttachmentRepository;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * 공지사항 첨부파일 관련 서비스 클래스
 *
 * <p>이 클래스는 다음 기능을 제공합니다:</p>
 * <ul>
 *     <li>SFTP 서버로 첨부파일 업로드</li>
 *     <li>공지사항 ID 기준 첨부파일 조회</li>
 * </ul>
 */
@Service
@Transactional
public class NoticeAttachmentService {

    private static final String SFTP_HOST = "ict05.wwwbiz.kr";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "ict05";
    private static final String SFTP_PASSWORD = "admin*1472";
    private static final String SFTP_BASE_DIR = "/httpdocs/"; // 서버 내 업로드 경로
    private static final String URL_BASE = "http://ict05.wwwbiz.kr";

    private final NoticeAttachmentRepository noticeAttachmentRepository;

    public NoticeAttachmentService(NoticeAttachmentRepository noticeAttachmentRepository) {
        this.noticeAttachmentRepository = noticeAttachmentRepository;
    }

    /**
     * 첨부파일을 SFTP 서버에 업로드하고 접근 가능한 URL을 반환한다.
     *
     * <p>파일명은 UUID 기반으로 생성하며 원본 확장자를 유지한다.
     * 업로드 후 파일 URL을 반환하며, 업로드 실패 시 예외 발생.</p>
     *
     * @param file 업로드할 MultipartFile 객체
     * @return 업로드된 파일 URL
     * @throws Exception SFTP 연결 실패, 업로드 실패 등 예외 발생
     */
    public String uploadFile(MultipartFile file) throws Exception {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            // JSCH 세션 생성
            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no"); // 키 체크 비활성화
            session.setConfig(config);
            session.connect();

            // SFTP 채널 열기
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // 서버 디렉토리 이동 (없으면 생성)
            try {
                channelSftp.cd(SFTP_BASE_DIR);
            } catch (Exception e) {
                channelSftp.mkdir(SFTP_BASE_DIR);
                channelSftp.cd(SFTP_BASE_DIR);
            }

            // 파일 이름 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + ext;

            // 파일 업로드
            InputStream inputStream = file.getInputStream();
            channelSftp.put(inputStream, newFileName);
            inputStream.close();

            // 업로드된 URL 반환
            return URL_BASE + "/" + newFileName;

        } finally {
            if (channelSftp != null) channelSftp.exit();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * 공지사항 ID를 기준으로 첨부파일 목록을 조회한다.
     *
     * @param noticeId 조회할 공지사항 ID
     * @return 해당 공지사항에 첨부된 파일 목록
     */
    public List<NoticeAttachment> findByNoticeId(Long noticeId) {
        List<NoticeAttachment> lists = noticeAttachmentRepository.findByNoticeId(noticeId);
        return lists;
    }


    public void deleteAttachment(Long id) {
        noticeAttachmentRepository.deleteById(id);
    }
}
