INSERT INTO manufacturer (id, name)
VALUES
    (1, 'Toyota'),
    (2, 'BMW');

INSERT INTO car_model (id, name, manufacturer_id)
VALUES
    (11, 'Corolla', 1),
    (12, 'Camry', 1),
    (21, 'X5', 2);

INSERT INTO category (id, name)
VALUES
    (31, 'Sedan'),
    (32, 'Hybrid'),
    (33, 'SUV');

INSERT INTO car (id, year, model_id)
VALUES
    (101, 2020, 11),
    (102, 2021, 12),
    (103, 2022, 11),
    (104, 2020, 21);

INSERT INTO car_category (car_id, category_id)
VALUES
    (101, 31),
    (101, 32),
    (102, 31),
    (104, 33);