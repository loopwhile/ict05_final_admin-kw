package com.boot.ict05_final_admin.domain.fcm.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoticeFcmEvent {

    public enum Type {
        CREATED,
        UPDATED
    }

    private final Long noticeId;
    private final Type type;
}
