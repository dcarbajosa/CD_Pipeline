CREATE TABLE EMPLOYEE(
    id INT NOT NULL auto_increment, 
    name VARCHAR(50) NOT NULL,
    joining_date DATE NOT NULL,
    salary DOUBLE NOT NULL,
    ssn VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

INSERT INTO EMPLOYEE ( id, name, joining_date, salary, ssn ) VALUES ( uuid(), 'Jane Smith', '10/08/2002', 5000.00, '11111111111' );
INSERT INTO EMPLOYEE ( id, name, joining_date, salary, ssn ) VALUES ( uuid(), 'Dave Richards','10/08/2003', 15000.00, '11111111112' );
