package com.boot.ict05_final_admin.domain.staffresources.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStaffProfile is a Querydsl query type for StaffProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStaffProfile extends EntityPathBase<StaffProfile> {

    private static final long serialVersionUID = 1870174014L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStaffProfile staffProfile = new QStaffProfile("staffProfile");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath staffAddress = createString("staffAddress");

    public final DateTimePath<java.time.LocalDateTime> staffBirth = createDateTime("staffBirth", java.time.LocalDateTime.class);

    public final EnumPath<StaffDepartment> staffDepartment = createEnum("staffDepartment", StaffDepartment.class);

    public final StringPath staffEmail = createString("staffEmail");

    public final EnumPath<StaffEmploymentType> staffEmploymentType = createEnum("staffEmploymentType", StaffEmploymentType.class);

    public final DateTimePath<java.time.LocalDateTime> staffEndDate = createDateTime("staffEndDate", java.time.LocalDateTime.class);

    public final StringPath staffName = createString("staffName");

    public final StringPath staffPhone = createString("staffPhone");

    public final NumberPath<Double> staffSalary = createNumber("staffSalary", Double.class);

    public final DateTimePath<java.time.LocalDateTime> staffStartDate = createDateTime("staffStartDate", java.time.LocalDateTime.class);

    public final com.boot.ict05_final_admin.domain.store.entity.QStore store;

    public QStaffProfile(String variable) {
        this(StaffProfile.class, forVariable(variable), INITS);
    }

    public QStaffProfile(Path<? extends StaffProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStaffProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStaffProfile(PathMetadata metadata, PathInits inits) {
        this(StaffProfile.class, metadata, inits);
    }

    public QStaffProfile(Class<? extends StaffProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.boot.ict05_final_admin.domain.store.entity.QStore(forProperty("store")) : null;
    }

}

