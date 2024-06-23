CREATE TABLE ADDRESS (
    address_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    premises        VARCHAR(50),
    address_line_1  VARCHAR(50),
    locality        VARCHAR(50),
    postal_code     VARCHAR(50),
    country         VARCHAR(50)
);

CREATE TABLE COMPANY (
  company_number    VARCHAR(20) PRIMARY KEY,
  title             VARCHAR(250),
  company_type      VARCHAR(50),
  company_status    VARCHAR(50),
  date_of_creation  VARCHAR(10),
  address_id        BIGINT,
  FOREIGN KEY (address_id) REFERENCES ADDRESS(address_id)
  );

CREATE TABLE OFFICER (
  officer_id        BIGINT GENERATED ALWAYS AS  IDENTITY PRIMARY KEY,
  company_number    VARCHAR(20) NOT NULL,
  name              VARCHAR(250),
  officer_role      VARCHAR(50),
  appointed_on      VARCHAR(10),
  address_id        BIGINT,
  FOREIGN KEY (address_id) REFERENCES ADDRESS(address_id)
);


