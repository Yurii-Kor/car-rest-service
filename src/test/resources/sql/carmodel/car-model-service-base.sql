INSERT INTO manufacturer (id, name)
VALUES
    (101, 'Toyota'),
    (102, 'Honda'),
    (103, 'BMW'),
    (104, 'Empty Motors');

INSERT INTO car_model (id, name, manufacturer_id)
VALUES
    (201, 'Corolla', 101),
    (202, 'Camry', 101),
    (203, 'Civic', 102),
    (204, 'Corolla', 102),
    (205, 'X5', 103);

INSERT INTO car (id, year, model_id)
VALUES
    (301, 2020, 201);
