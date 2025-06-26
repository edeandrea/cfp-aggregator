		-- Sequences
    create sequence event_seq start with 1 increment by 1;
    create sequence speaker_seq start with 1 increment by 1;
    create sequence talk_seq start with 1 increment by 1;

		-- Tables
    create table events (
        cfp_closing timestamp(6) with time zone,
        cfp_opening timestamp(6) with time zone,
        from_date timestamp(6) with time zone,
        id bigint not null,
        to_date timestamp(6) with time zone,
        description varchar(255),
        flickr_url varchar(255),
        name varchar(255) not null,
        time_zone varchar(255),
        website_url varchar(255),
        you_tube_url varchar(255),
        portal_type enum ('CFP_DEV','DEV2NEXT','SESSIONIZE') not null,
        primary key (id)
    );

    create table speaker_talks (
        speaker_id bigint not null,
        talk_id bigint not null
    );

    create table speakers (
        event_id bigint not null,
        id bigint not null,
        bio TEXT,
        bluesky_username varchar(255),
        company varchar(255),
        country_name varchar(255),
        first_name varchar(255) not null,
        image_url varchar(255),
        last_name varchar(255) not null,
        linked_in_username varchar(255),
        twitter_handle varchar(255),
        primary key (id)
    );

    create table talks (
        id bigint not null,
        description varchar(255),
        title varchar(255) not null,
        video_url varchar(255),
        primary key (id)
    );

		-- Foreign keys
    alter table if exists speaker_talks 
       add constraint FKbqpjb2jdes2vmpywymqr2pc97 
       foreign key (talk_id) 
       references talks;

    alter table if exists speaker_talks 
       add constraint FK6x1lw682i95lpm23nntpg6bp2 
       foreign key (speaker_id) 
       references speakers;

    alter table if exists speakers 
       add constraint FKssjp34bs2oghuq689b5liqlpn 
       foreign key (event_id) 
       references events;
