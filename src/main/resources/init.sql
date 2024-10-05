DROP TABLE IF EXISTS file;
CREATE TABLE file
(
    id          uuid PRIMARY KEY,
    file_path   varchar,
    is_uploaded boolean
);
