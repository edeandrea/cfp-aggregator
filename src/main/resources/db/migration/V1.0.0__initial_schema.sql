-- Drop tables and sequences
drop table if exists events cascade;
drop table if exists portals cascade;
drop table if exists speaker_talks cascade;
drop table if exists speakers cascade;
drop table if exists talks cascade;
drop sequence if exists speaker_seq;
drop sequence if exists talk_seq;

-- Create sequences
create sequence speaker_seq start with 1 increment by 1;
create sequence talk_seq start with 1 increment by 1;

-- Create tables
create table events (
		portal_name varchar(255) not null,
		cfp_closing timestamp(6) with time zone,
		cfp_opening timestamp(6) with time zone,
		description TEXT,
		flickr_url varchar(255),
		from_date timestamp(6) with time zone,
		name varchar(255) not null,
		time_zone varchar(255),
		to_date timestamp(6) with time zone,
		website_url TEXT,
		you_tube_url TEXT,
		primary key (portal_name)
);

create table portals (
		portal_name varchar(255) not null,
		base_url varchar(255) not null,
		description TEXT,
		portal_type enum ('CFP_DEV','DEV2NEXT','SESSIONIZE') not null,
		primary key (portal_name)
);

create table speaker_talks (
		speaker_id bigint not null,
		talk_id bigint not null
);

create table speakers (
		id bigint not null,
		bio TEXT,
		bluesky_username varchar(255),
		company varchar(255),
		country_name varchar(255),
		event_speaker_id varchar(255) not null,
		first_name varchar(255) not null,
		image_url TEXT,
		last_name varchar(255) not null,
		linked_in_username varchar(255),
		twitter_handle varchar(255),
		event_portal_name varchar(255) not null,
		primary key (id)
);

create table talks (
		id bigint not null,
		description TEXT,
		event_talk_id varchar(255) not null,
		summary TEXT,
		title varchar(255) not null,
		video_url TEXT,
		primary key (id)
);

-- Create FKs
alter table if exists events
	 add constraint events_portals_portal_name_fk
	 foreign key (portal_name)
	 references portals;

alter table if exists speaker_talks
	 add constraint speaker_talks_talks_talk_id_fk
	 foreign key (talk_id)
	 references talks;

alter table if exists speaker_talks
	 add constraint speaker_talks_speakers_speaker_id_fk
	 foreign key (speaker_id)
	 references speakers;

alter table if exists speakers
	 add constraint speakers_events_portal_name_fk
	 foreign key (event_portal_name)
	 references events;
