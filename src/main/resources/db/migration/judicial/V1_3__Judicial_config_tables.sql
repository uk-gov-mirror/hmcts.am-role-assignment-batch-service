CREATE TABLE booking(
	id uuid NOT NULL,
	user_id text NOT NULL,
	appointment_id text NOT NULL,
	role_id text NOT NULL,
	contract_type_id text NOT NULL,
	base_location_id text,
	region_id text,
	status text NOT NULL,
	begin_time timestamp NOT NULL,
	end_time timestamp NOT NULL,
	created timestamp NOT NULL,
	log text,
	CONSTRAINT booking_pkey PRIMARY KEY (id)
);