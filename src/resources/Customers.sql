SELECT * FROM customer;
SELECT * FROM address;
SELECT * FROM city;
SELECT * FROM country;

SELECT	countryId
FROM	country
WHERE	country = '?';
INSERT INTO country (	country,
						createdBy,
                        lastUpdateBy )
VALUES (?, ?, ?);
SELECT max(countryId) FROM country;

INSERT INTO city (	city,
					countryId,
                    createdBy,
                    lastUpdateBy )
VALUES (?, ?, ?, ?);
SELECT max(cityId) FROM city;

INSERT INTO Address (	address,
						address2,
                        cityId,
                        postalCode,
                        phone,
                        createdBy,
                        lastUpdateBy )
VALUES	(?, ?, ?, ?, ?, ?, ?);
SELECT max(addressId) FROM address;

INSERT INTO customer (	customerName,
						addressId,
                        active,
                        createdBy,
                        lastUpdatedBy )
VALUES (?, ?, ?, ?, ?);

SELECT	c.customerId,
		c.customerName,
        a.address,
        a.address2,
        ct.city,
        ctr.country,
        a.postalCode,
        a.phone,
        c.active
FROM	customer c JOIN
		address a ON c.addressId = a.addressId JOIN
        city ct ON a.cityId = ct.cityId JOIN
        country ctr ON ct.countryId = ctr.countryId;
        
SELECT * FROM appointment;

        