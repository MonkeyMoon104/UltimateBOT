create table licenses (
    id uuid primary key,
    license_key_hmac varchar(64) not null unique,
    license_key_prefix varchar(4) not null,
    customer_id varchar(128) not null,
    product_code varchar(64) not null,
    plan_code varchar(64) not null,
    status varchar(32) not null,
    expires_at timestamptz null,
    max_servers integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    revoked_reason varchar(255) null
);

create index idx_licenses_prefix on licenses (license_key_prefix);
create index idx_licenses_status on licenses (status);

create table license_activations (
    id uuid primary key,
    license_id uuid not null references licenses(id) on delete cascade,
    installation_id varchar(64) not null,
    fingerprint_hash varchar(64) not null,
    first_seen_at timestamptz not null,
    last_seen_at timestamptz not null,
    last_ip varchar(64) null,
    last_plugin_version varchar(64) null,
    status varchar(32) not null
);

create unique index uq_license_activations_license_installation
    on license_activations (license_id, installation_id);
create index idx_license_activations_last_seen
    on license_activations (license_id, last_seen_at);

create table license_events (
    id uuid primary key,
    license_id uuid null references licenses(id) on delete set null,
    installation_id varchar(64) null,
    event_type varchar(32) not null,
    result varchar(32) not null,
    reason_code varchar(64) null,
    message varchar(255) null,
    ip varchar(64) null,
    created_at timestamptz not null
);

create index idx_license_events_license_created
    on license_events (license_id, created_at desc);
