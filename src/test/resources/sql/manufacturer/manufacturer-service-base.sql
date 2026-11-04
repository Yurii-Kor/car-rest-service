INSERT INTO manufacturer (id, name)
VALUES
    (101, 'Toyota'),
    (102, 'BMW'),
    (103, 'Unused Motors'),
    (104, 'Prototype Motors');

INSERT INTO car_model (id, name, manufacturer_id)
VALUES
    (201, 'Corolla', 101),
    (202, 'Concept', 104);

INSERT INTO car (id, year, model_id)
VALUES
    (301, 2020, 201);
