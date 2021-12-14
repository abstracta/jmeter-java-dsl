CREATE TABLE users(
  id SERIAL,
  name VARCHAR(255),
  age INT,
  address VARCHAR(500)
);

INSERT INTO users(name, age, address) VALUES
('User1', 21, 'My address1'),
('User2', 22, 'My address2');

CREATE OR REPLACE FUNCTION incr(INOUT num INT)
    RETURNS INT AS '
BEGIN
  num := num + 1;
END;
'
LANGUAGE plpgsql;
