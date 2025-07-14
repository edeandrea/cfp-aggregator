drop table if exists events cascade;
drop table if exists portals cascade;
drop table if exists speaker_talks cascade;
drop table if exists speakers cascade;
drop table if exists talks cascade ;
drop sequence if exists speaker_seq;
drop sequence if exists talk_seq;

create sequence speaker_seq start with 1 increment by 1;

    create sequence talk_seq start with 1 increment by 1;

    create table events (
        cfp_closing timestamp(6) with time zone,
        cfp_opening timestamp(6) with time zone,
        from_date timestamp(6) with time zone,
        to_date timestamp(6) with time zone,
        description TEXT,
        flickr_url varchar(255),
        name varchar(255) not null,
        portal_name varchar(255) not null,
        time_zone varchar(255),
        website_url varchar(255),
        you_tube_url varchar(255),
        primary key (portal_name)
    );

    create table portals (
        base_url varchar(255) not null,
        description TEXT,
        portal_name varchar(255) not null,
        portal_type enum ('CFP_DEV','DEV2NEXT','SESSIONIZE') not null,
        primary key (portal_name)
    );

    create table speaker_talks (
        speaker_id bigint not null,
        talk_id bigint not null
    );

    create table speakers (
        event_speaker_id bigint not null,
        id bigint not null,
        bio TEXT,
        bluesky_username varchar(255),
        company varchar(255),
        country_name varchar(255),
        event_portal_name varchar(255) not null,
        first_name varchar(255) not null,
        image_url varchar(255),
        last_name varchar(255) not null,
        linked_in_username varchar(255),
        twitter_handle varchar(255),
        primary key (id)
    );

    create table talks (
        event_talk_id bigint not null,
        id bigint not null,
        description TEXT,
        summary TEXT,
        title varchar(255) not null,
        video_url varchar(255),
        primary key (id)
    );

    alter table if exists events
       add constraint FKqfmn6t9bf4ndtrjis0tlv4hgv
       foreign key (portal_name)
       references portals;

    alter table if exists speaker_talks
       add constraint FKbqpjb2jdes2vmpywymqr2pc97
       foreign key (talk_id)
       references talks;

    alter table if exists speaker_talks
       add constraint FK6x1lw682i95lpm23nntpg6bp2
       foreign key (speaker_id)
       references speakers;

    alter table if exists speakers
       add constraint FKm02nf54s4ipg987m5xx2lfpqu
       foreign key (event_portal_name)
       references events;
