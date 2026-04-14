create table plugin_versions (
    id uuid primary key,
    product_code varchar(64) not null,
    version_name varchar(64) not null,
    release_url varchar(255) not null,
    is_latest boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index uq_plugin_versions_product_version
    on plugin_versions (product_code, version_name);

create index idx_plugin_versions_product_latest
    on plugin_versions (product_code, is_latest);
