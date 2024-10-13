DROP TABLE IF EXISTS file;
CREATE TABLE file
(
    uuid uuid NOT NULL PRIMARY KEY,
    file_path   varchar,
    is_uploaded boolean
);
