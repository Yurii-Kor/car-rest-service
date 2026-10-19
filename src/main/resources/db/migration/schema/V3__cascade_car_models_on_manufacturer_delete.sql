ALTER TABLE car_model
    DROP CONSTRAINT fk_car_model_manufacturer;

ALTER TABLE car_model
    ADD CONSTRAINT fk_car_model_manufacturer
    FOREIGN KEY (manufacturer_id)
    REFERENCES manufacturer (id)
    ON DELETE CASCADE;