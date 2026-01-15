--
-- PostgreSQL database dump
--

\restrict dr9vyrPO1TENRKgEsFAMxvUCnaM65V9mlWMJ1R37LFCIbMVrKG7Y4urKHiAr7dx

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: spycon
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO spycon;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.categories (
    category_id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.categories OWNER TO spycon;

--
-- Name: categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.categories_category_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categories_category_id_seq OWNER TO spycon;

--
-- Name: categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.categories_category_id_seq OWNED BY public.categories.category_id;


--
-- Name: inventory_logs; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.inventory_logs (
    log_id integer NOT NULL,
    product_id integer NOT NULL,
    change_amount integer NOT NULL,
    previous_quantity integer NOT NULL,
    new_quantity integer NOT NULL,
    change_type character varying(50) NOT NULL,
    change_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    reason text,
    performed_by integer,
    CONSTRAINT chk_change_type CHECK (((change_type)::text = ANY ((ARRAY['restock'::character varying, 'sale'::character varying, 'adjustment'::character varying, 'return'::character varying, 'damaged'::character varying])::text[])))
);


ALTER TABLE public.inventory_logs OWNER TO spycon;

--
-- Name: inventory_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.inventory_logs_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventory_logs_log_id_seq OWNER TO spycon;

--
-- Name: inventory_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.inventory_logs_log_id_seq OWNED BY public.inventory_logs.log_id;


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.order_items (
    id integer NOT NULL,
    order_id integer NOT NULL,
    product_id integer NOT NULL,
    quantity integer NOT NULL,
    price_at_purchase numeric(10,2) NOT NULL,
    CONSTRAINT order_items_price_at_purchase_check CHECK ((price_at_purchase >= (0)::numeric)),
    CONSTRAINT order_items_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.order_items OWNER TO spycon;

--
-- Name: order_items_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.order_items_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.order_items_id_seq OWNER TO spycon;

--
-- Name: order_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.order_items_id_seq OWNED BY public.order_items.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.orders (
    order_id integer NOT NULL,
    user_id integer NOT NULL,
    order_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(50) DEFAULT 'pending'::character varying NOT NULL,
    total_amount numeric(10,2) NOT NULL,
    shipping_address text,
    CONSTRAINT chk_status CHECK (((status)::text = ANY ((ARRAY['pending'::character varying, 'processing'::character varying, 'shipped'::character varying, 'delivered'::character varying, 'cancelled'::character varying])::text[]))),
    CONSTRAINT orders_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


ALTER TABLE public.orders OWNER TO spycon;

--
-- Name: orders_order_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.orders_order_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.orders_order_id_seq OWNER TO spycon;

--
-- Name: orders_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.orders_order_id_seq OWNED BY public.orders.order_id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.products (
    product_id integer NOT NULL,
    category_id integer,
    name character varying(255) NOT NULL,
    description text,
    price numeric(10,2) NOT NULL,
    stock_quantity integer DEFAULT 0 NOT NULL,
    sku character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT products_price_check CHECK ((price >= (0)::numeric)),
    CONSTRAINT products_stock_quantity_check CHECK ((stock_quantity >= 0))
);


ALTER TABLE public.products OWNER TO spycon;

--
-- Name: products_product_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.products_product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_product_id_seq OWNER TO spycon;

--
-- Name: products_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.products_product_id_seq OWNED BY public.products.product_id;


--
-- Name: reviews; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.reviews (
    review_id integer NOT NULL,
    product_id integer NOT NULL,
    user_id integer NOT NULL,
    rating integer NOT NULL,
    title character varying(255),
    comment text,
    is_verified_purchase boolean DEFAULT false,
    helpful_votes integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


ALTER TABLE public.reviews OWNER TO spycon;

--
-- Name: reviews_review_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.reviews_review_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reviews_review_id_seq OWNER TO spycon;

--
-- Name: reviews_review_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.reviews_review_id_seq OWNED BY public.reviews.review_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: spycon
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    first_name character varying(100),
    last_name character varying(100),
    role character varying(50) DEFAULT 'customer'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (((role)::text = ANY ((ARRAY['admin'::character varying, 'customer'::character varying, 'manager'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO spycon;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: spycon
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO spycon;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: spycon
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: categories category_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.categories ALTER COLUMN category_id SET DEFAULT nextval('public.categories_category_id_seq'::regclass);


--
-- Name: inventory_logs log_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.inventory_logs ALTER COLUMN log_id SET DEFAULT nextval('public.inventory_logs_log_id_seq'::regclass);


--
-- Name: order_items id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.order_items ALTER COLUMN id SET DEFAULT nextval('public.order_items_id_seq'::regclass);


--
-- Name: orders order_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.orders ALTER COLUMN order_id SET DEFAULT nextval('public.orders_order_id_seq'::regclass);


--
-- Name: products product_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.products_product_id_seq'::regclass);


--
-- Name: reviews review_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.reviews ALTER COLUMN review_id SET DEFAULT nextval('public.reviews_review_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.categories (category_id, name, description, created_at) FROM stdin;
1	General	\N	2026-01-15 19:03:43.969955
2	Electronics	Electronic devices and gadgets including computers, phones, and accessories	2026-01-15 19:05:19.891291
3	Books	Physical and digital books across all genres	2026-01-15 19:05:19.891291
4	Clothing	Apparel for men, women, and children	2026-01-15 19:05:19.891291
5	Home & Kitchen	Furniture, appliances, and kitchen essentials	2026-01-15 19:05:19.891291
6	Sports & Outdoors	Sports equipment and outdoor gear	2026-01-15 19:05:19.891291
7	Beauty & Health	Cosmetics, skincare, and health products	2026-01-15 19:05:19.891291
8	Toys & Games	Toys, board games, and video games	2026-01-15 19:05:19.891291
9	Office Supplies	Stationery and office equipment	2026-01-15 19:05:19.891291
\.


--
-- Data for Name: inventory_logs; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.inventory_logs (log_id, product_id, change_amount, previous_quantity, new_quantity, change_type, change_date, reason, performed_by) FROM stdin;
1	1	50	0	50	restock	2026-01-15 19:05:19.891291	Initial inventory	1
2	1	-25	50	25	sale	2026-01-15 19:05:19.891291	January sales	2
3	2	100	0	100	restock	2026-01-15 19:05:19.891291	Initial inventory	1
4	2	-50	100	50	sale	2026-01-15 19:05:19.891291	Holiday promotion sales	2
5	3	150	0	150	restock	2026-01-15 19:05:19.891291	Initial inventory	1
6	3	-50	150	100	sale	2026-01-15 19:05:19.891291	Black Friday sales	2
7	15	-5	205	200	damaged	2026-01-15 19:05:19.891291	Warehouse damage	2
8	20	30	20	50	restock	2026-01-15 19:05:19.891291	Supplier shipment received	1
\.


--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.order_items (id, order_id, product_id, quantity, price_at_purchase) FROM stdin;
1	1	1	1	1299.99
2	1	7	1	49.99
3	2	3	1	199.99
4	2	7	1	49.99
5	3	2	1	899.99
6	4	15	3	19.99
7	4	21	2	44.99
8	4	24	1	24.99
9	5	22	1	89.99
10	6	4	1	349.99
11	6	3	1	199.99
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.orders (order_id, user_id, order_date, status, total_amount, shipping_address) FROM stdin;
1	3	2026-01-15 19:05:19.891291	delivered	1349.98	123 Main St, New York, NY 10001
2	3	2026-01-15 19:05:19.891291	shipped	249.98	123 Main St, New York, NY 10001
3	4	2026-01-15 19:05:19.891291	delivered	899.99	456 Oak Ave, Los Angeles, CA 90001
4	5	2026-01-15 19:05:19.891291	processing	179.97	789 Pine Rd, Chicago, IL 60601
5	6	2026-01-15 19:05:19.891291	pending	89.99	321 Elm St, Houston, TX 77001
6	7	2026-01-15 19:05:19.891291	delivered	549.97	654 Maple Dr, Phoenix, AZ 85001
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.products (product_id, category_id, name, description, price, stock_quantity, sku, created_at, updated_at) FROM stdin;
1	1	Laptop Pro 15	High-performance laptop with 15-inch display, 16GB RAM, 512GB SSD	1299.99	25	ELEC-LAP-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
2	1	Smartphone X12	Latest smartphone with 6.5-inch OLED, 128GB storage	899.99	50	ELEC-PHN-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
3	1	Wireless Earbuds	Premium noise-cancelling wireless earbuds	199.99	100	ELEC-AUD-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
4	1	Smart Watch Pro	Fitness tracking smartwatch with heart rate monitor	349.99	40	ELEC-WTC-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
5	1	4K Monitor 27"	Ultra HD monitor with HDR support	449.99	30	ELEC-MON-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
6	1	Mechanical Keyboard	RGB mechanical gaming keyboard	129.99	75	ELEC-KEY-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
7	1	Wireless Mouse	Ergonomic wireless mouse with long battery life	49.99	150	ELEC-MOU-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
8	1	USB-C Hub	7-in-1 USB-C hub with HDMI and card reader	59.99	200	ELEC-HUB-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
9	2	Java Programming Masterclass	Comprehensive guide to Java programming	49.99	100	BOOK-PRG-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
10	2	Data Structures & Algorithms	Essential algorithms for software developers	54.99	80	BOOK-PRG-002	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
11	2	Clean Code	A handbook of agile software craftsmanship	39.99	120	BOOK-PRG-003	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
12	2	Design Patterns	Elements of reusable object-oriented software	44.99	60	BOOK-PRG-004	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
13	2	The Art of Fiction	Classic guide to creative writing	24.99	90	BOOK-FIC-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
14	2	World History Encyclopedia	Comprehensive world history reference	79.99	40	BOOK-REF-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
15	3	Cotton T-Shirt	Premium cotton crew neck t-shirt	19.99	200	CLTH-TSH-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
16	3	Denim Jeans	Classic fit denim jeans	59.99	150	CLTH-JNS-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
17	3	Hoodie Sweatshirt	Comfortable pullover hoodie	44.99	100	CLTH-HOD-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
18	3	Running Shoes	Lightweight athletic running shoes	89.99	80	CLTH-SHO-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
19	3	Winter Jacket	Insulated winter jacket with hood	129.99	60	CLTH-JKT-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
20	3	Sports Cap	Adjustable sports baseball cap	24.99	250	CLTH-CAP-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
21	4	Coffee Maker	Programmable 12-cup coffee maker	79.99	45	HOME-COF-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
22	4	Blender Pro	High-speed blender with multiple settings	99.99	35	HOME-BLN-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
23	4	Air Fryer	Digital air fryer with 5-quart capacity	119.99	50	HOME-FRY-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
24	4	Knife Set	Professional 8-piece knife block set	149.99	30	HOME-KNF-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
25	4	Bed Sheet Set	Egyptian cotton queen bed sheet set	69.99	70	HOME-BED-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
26	5	Yoga Mat	Non-slip exercise yoga mat	29.99	150	SPRT-YOG-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
27	5	Dumbbell Set	Adjustable dumbbell set 5-25 lbs	199.99	40	SPRT-DUM-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
28	5	Camping Tent	4-person waterproof camping tent	149.99	25	SPRT-TNT-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
29	5	Hiking Backpack	40L hiking backpack with rain cover	89.99	55	SPRT-BAG-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
30	5	Bicycle Helmet	Adjustable safety bicycle helmet	49.99	80	SPRT-HLM-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
31	6	Face Moisturizer	Daily hydrating face moisturizer	34.99	120	BEAU-MOI-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
32	6	Vitamin C Serum	Brightening vitamin C face serum	44.99	90	BEAU-SER-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
33	6	Electric Toothbrush	Rechargeable sonic toothbrush	69.99	60	HEAL-TBR-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
34	6	Protein Powder	Whey protein powder 2lb container	39.99	100	HEAL-PRO-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
35	7	Building Blocks Set	500-piece creative building blocks	29.99	80	TOYS-BLK-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
36	7	Board Game Collection	Classic family board game collection	39.99	50	TOYS-BRD-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
37	7	Remote Control Car	Off-road RC car with rechargeable battery	59.99	45	TOYS-RCC-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
38	7	Puzzle 1000pc	1000-piece landscape jigsaw puzzle	19.99	100	TOYS-PZL-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
39	8	Notebook Pack	Pack of 5 spiral notebooks	14.99	200	OFFC-NBK-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
40	8	Pen Set	Premium ballpoint pen set of 12	9.99	300	OFFC-PEN-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
41	8	Desk Organizer	Multi-compartment desk organizer	29.99	80	OFFC-ORG-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
42	8	Printer Paper	500-sheet printer paper ream	8.99	500	OFFC-PPR-001	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
\.


--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.reviews (review_id, product_id, user_id, rating, title, comment, is_verified_purchase, helpful_votes, created_at, updated_at) FROM stdin;
1	1	3	5	Excellent Laptop!	This laptop exceeded my expectations. Fast, sleek, and great battery life. Highly recommend for developers!	t	15	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
2	1	4	4	Great but pricey	Solid performance but a bit expensive. Still worth it for the quality.	t	8	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
3	2	4	5	Best phone I have owned	Amazing camera quality and the display is gorgeous. Battery lasts all day.	t	22	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
4	2	5	4	Good phone	Nice phone overall. Some features are confusing but great value.	f	5	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
5	3	3	5	Crystal clear audio	The noise cancellation is incredible. Perfect for commuting.	t	12	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
6	9	6	5	Must-read for Java devs	Comprehensive and well-written. Helped me land my first job!	t	30	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
7	11	7	4	Life-changing book	Really improved my code quality. Some examples are dated but concepts are solid.	t	18	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
8	15	5	3	Decent quality	The fabric is nice but shrunk after washing. Size up!	t	6	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
9	25	8	5	Perfect for home gym	Great quality dumbbells. The adjustable feature is smooth and reliable.	t	9	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
10	27	6	4	Good coffee maker	Makes great coffee. The programmable timer is convenient.	t	7	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: spycon
--

COPY public.users (user_id, email, password_hash, first_name, last_name, role, created_at, updated_at) FROM stdin;
1	admin@ecommerce.com	hashed_admin_password_123	Admin	User	admin	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
2	manager@ecommerce.com	hashed_manager_password_123	Store	Manager	manager	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
3	john.doe@email.com	hashed_password_456	John	Doe	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
4	jane.smith@email.com	hashed_password_789	Jane	Smith	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
5	bob.wilson@email.com	hashed_password_012	Bob	Wilson	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
6	alice.brown@email.com	hashed_password_345	Alice	Brown	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
7	charlie.davis@email.com	hashed_password_678	Charlie	Davis	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
8	diana.miller@email.com	hashed_password_901	Diana	Miller	customer	2026-01-15 19:05:19.891291	2026-01-15 19:05:19.891291
\.


--
-- Name: categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.categories_category_id_seq', 9, true);


--
-- Name: inventory_logs_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.inventory_logs_log_id_seq', 8, true);


--
-- Name: order_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.order_items_id_seq', 11, true);


--
-- Name: orders_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.orders_order_id_seq', 6, true);


--
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.products_product_id_seq', 42, true);


--
-- Name: reviews_review_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.reviews_review_id_seq', 10, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: spycon
--

SELECT pg_catalog.setval('public.users_user_id_seq', 8, true);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (category_id);


--
-- Name: inventory_logs inventory_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.inventory_logs
    ADD CONSTRAINT inventory_logs_pkey PRIMARY KEY (log_id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- Name: products products_sku_key; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_sku_key UNIQUE (sku);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (review_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: idx_inventory_logs_date; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_inventory_logs_date ON public.inventory_logs USING btree (change_date);


--
-- Name: idx_inventory_logs_product; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_inventory_logs_product ON public.inventory_logs USING btree (product_id);


--
-- Name: idx_inventory_logs_type; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_inventory_logs_type ON public.inventory_logs USING btree (change_type);


--
-- Name: idx_order_items_order; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_order_items_order ON public.order_items USING btree (order_id);


--
-- Name: idx_order_items_product; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_order_items_product ON public.order_items USING btree (product_id);


--
-- Name: idx_orders_date; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_orders_date ON public.orders USING btree (order_date);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_user; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_orders_user ON public.orders USING btree (user_id);


--
-- Name: idx_products_category; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_products_category ON public.products USING btree (category_id);


--
-- Name: idx_products_name; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_products_name ON public.products USING btree (name);


--
-- Name: idx_products_name_lower; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_products_name_lower ON public.products USING btree (lower((name)::text));


--
-- Name: idx_products_price; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_products_price ON public.products USING btree (price);


--
-- Name: idx_products_sku; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_products_sku ON public.products USING btree (sku);


--
-- Name: idx_reviews_product; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_reviews_product ON public.reviews USING btree (product_id);


--
-- Name: idx_reviews_rating; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_reviews_rating ON public.reviews USING btree (rating);


--
-- Name: idx_reviews_user; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_reviews_user ON public.reviews USING btree (user_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: spycon
--

CREATE INDEX idx_users_role ON public.users USING btree (role);


--
-- Name: products update_products_updated_at; Type: TRIGGER; Schema: public; Owner: spycon
--

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON public.products FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: reviews update_reviews_updated_at; Type: TRIGGER; Schema: public; Owner: spycon
--

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON public.reviews FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: users update_users_updated_at; Type: TRIGGER; Schema: public; Owner: spycon
--

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: inventory_logs inventory_logs_performed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.inventory_logs
    ADD CONSTRAINT inventory_logs_performed_by_fkey FOREIGN KEY (performed_by) REFERENCES public.users(user_id) ON DELETE SET NULL;


--
-- Name: inventory_logs inventory_logs_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.inventory_logs
    ADD CONSTRAINT inventory_logs_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE CASCADE;


--
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON DELETE CASCADE;


--
-- Name: order_items order_items_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE RESTRICT;


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: products products_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(category_id) ON DELETE SET NULL;


--
-- Name: reviews reviews_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE CASCADE;


--
-- Name: reviews reviews_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: spycon
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict dr9vyrPO1TENRKgEsFAMxvUCnaM65V9mlWMJ1R37LFCIbMVrKG7Y4urKHiAr7dx

