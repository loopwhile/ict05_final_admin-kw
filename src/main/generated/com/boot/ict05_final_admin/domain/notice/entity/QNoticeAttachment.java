package com.boot.ict05_final_admin.domain.notice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNoticeAttachment is a Querydsl query type for NoticeAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoticeAttachment extends EntityPathBase<NoticeAttachment> {

    private static final long serialVersionUID = 1029454275L;

    public static final QNoticeAttachment noticeAttachment = new QNoticeAttachment("noticeAttachment");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> noticeId = createNumber("noticeId", Long.class);

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath url = createString("url");

    public QNoticeAttachment(String variable) {
        super(NoticeAttachment.class, forVariable(variable));
    }

    public QNoticeAttachment(Path<? extends NoticeAttachment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNoticeAttachment(PathMetadata metadata) {
        super(NoticeAttachment.class, metadata);
    }

}

