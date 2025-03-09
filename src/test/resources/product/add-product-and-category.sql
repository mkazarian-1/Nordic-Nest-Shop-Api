INSERT INTO categories (id, title, description, image_url, type)
VALUES (1,'Sleeping room,Test category1','Cool big description1','https://nordic-nest-bucket.s3.amazonaws.com/cat1.webp','ROOM'),
       (2,'Sleeping room,Test category2','Cool big description2','https://nordic-nest-bucket.s3.amazonaws.com/cat2.webp','TYPE'),
       (3,'Sleeping room,Test category3','Cool big description3','https://nordic-nest-bucket.s3.amazonaws.com/cat3.webp','DESIGN'),
       (4,'Sleeping room,Test category4','Cool big description4','https://nordic-nest-bucket.s3.amazonaws.com/cat4.webp','TYPE'),
       (5,'Sleeping room,Test category5','Cool big description5','https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp','DESIGN');

INSERT INTO products (id, title, description, article, price)
VALUES (1,'Very Chear1','info about cool bad1','123-3440g1','10.00'),
       (2,'Very Cat Chear2','info about cool bad2','123-3440g1','130.00'),
       (3,'Very Chear3','info Table cool bad3','123-3440g2','140.00'),
       (4,'Very Chear Cat4','info about cool bad4','111-3440g3','110.00'),
       (5,'Chear5','info about cool bad5','123-3440g4','200.00'),
       (6,'Very Chear6','info Cat4 about cool bad6','123-3440g5','130.00');

INSERT INTO product_category (category_id, product_id)
VALUES (1,1),
       (2,1),
       (5,1),
       (1,2),
       (1,3),
       (3,5),
       (4,5);

INSERT INTO product_images (image_url, product_id, order_index)
VALUES ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp1-0',1,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp2-0',2,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp3-0',3,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp4-0',4,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp5-0',5,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp6-0',6,0),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp1-1',1,1),
       ('https://nordic-nest-bucket.s3.amazonaws.com/cat5.webp1-2',1,2);

INSERT INTO attributes (key, value, product_id)
VALUES ('size','l',1),
       ('size','l',2),
       ('size','m',3),
       ('size','s',4),
       ('size','xxl',5),
       ('size','s',6),
       ('color','green',1),
       ('color','black',2),
       ('color','black',6);
