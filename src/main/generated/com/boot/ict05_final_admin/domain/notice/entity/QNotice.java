package com.boot.ict05_final_admin.domain.notice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotice is a Querydsl query type for Notice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotice extends EntityPathBase<Notice> {

    private static final long serialVersionUID = 305570944L;

    public static final QNotice notice = new QNotice("notice");

    public final StringPath body = createString("body");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isShow = createBoolean("isShow");

    public final NumberPath<Long> memberIdFk = createNumber("memberIdFk", Long.class);

    public final EnumPath<NoticeCategory> noticeCategory = createEnum("noticeCategory", NoticeCategory.class);

    public final BooleanPath noticeConfirmed = createBoolean("noticeConfirmed");

    public final NumberPath<Integer> noticeCount = createNumber("noticeCount", Integer.class);

    public final EnumPath<NoticePriority> noticePriority = createEnum("noticePriority", NoticePriority.class);

    public final EnumPath<NoticeStatus> noticeStatus = createEnum("noticeStatus", NoticeStatus.class);

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final StringPath title = createString("title");

    public final StringPath writer = createString("writer");

    public QNotice(String variable) {
        super(Notice.class, forVariable(variable));
    }

    public QNotice(Path<? extends Notice> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotice(PathMetadata metadata) {
        super(Notice.class, metadata);
    }

}

