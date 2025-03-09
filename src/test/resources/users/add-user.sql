INSERT INTO users (id, email, first_name, second_name, password)
VALUES (2, 'Lanot@gmail.com-MANAGER', 'Jhon1', 'JoJhon1', '$2a$10$PMKQzPuRZBT3/csrtnjJGOdc69s0tUaJeAm7rZrwWTan9eyMINvAu'),
       (3, 'Lanot@gmail.com', 'Jhon2', 'JoJhon2', '$2a$10$PMKQzPuRZBT3/csrtnjJGOdc69s0tUaJeAm7rZrwWTan9eyMINvAu');

INSERT INTO user_roles (user_id, roles) VALUES (3, 'USER'),(2, 'USER'), (2,'ADMIN');
