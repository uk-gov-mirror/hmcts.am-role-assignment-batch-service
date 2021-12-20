CREATE TABLE booking(
	id uuid NOT NULL,
	user_id text NOT NULL,
	location_id text,
	region_id text,
	begin_time timestamp NOT NULL,
	end_time timestamp NOT NULL,
	created timestamp NOT NULL,
	CONSTRAINT booking_pkey PRIMARY KEY (id)
);