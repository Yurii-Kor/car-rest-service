INSERT INTO manufacturer (id, name)
VALUES
    (201, 'Toyota');

INSERT INTO car_model (id, name, manufacturer_id)
VALUES
    (301, 'Corolla', 201);

INSERT INTO car (id, year, model_id)
VALUES
    (401, 2020, 301);

INSERT INTO category (id, name)
VALUES
    (101, 'Sedan'),
    (102, 'Hatchback'),
    (103, 'Coupe'),
    (104, 'Truck');

INSERT INTO car_category (car_id, category_id)
VALUES
    (401, 101);
