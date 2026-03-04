package com.example.lankasmartmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "LankaSmartMart.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_ADDRESSES = "addresses";
    public static final String TABLE_CART = "cart";

    // USERS Table Columns
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "full_name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PHONE = "phone";
    public static final String USER_CREATED_AT = "created_at";

    // PRODUCTS Table Columns
    public static final String PRODUCT_ID = "product_id";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_CATEGORY_ID = "category_id";
    public static final String PRODUCT_DESCRIPTION = "description";
    public static final String PRODUCT_IMAGE = "image_url";
    public static final String PRODUCT_STOCK = "stock";

    // ORDERS Table Columns
    public static final String ORDER_ID = "order_id";
    public static final String ORDER_USER_ID = "user_id";
    public static final String ORDER_TOTAL = "total_price";
    public static final String ORDER_DATE = "order_date";
    public static final String ORDER_STATUS = "status";

    // ORDER ITEMS Table Columns
    public static final String ITEM_ID = "item_id";
    public static final String ITEM_ORDER_ID = "order_id";
    public static final String ITEM_PRODUCT_ID = "product_id";
    public static final String ITEM_QUANTITY = "quantity";
    public static final String ITEM_PRICE = "price";

    // CATEGORIES Table Columns
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String CATEGORY_ICON = "category_icon";

    // ADDRESSES Table Columns
    public static final String ADDRESS_ID = "address_id";
    public static final String ADDRESS_USER_ID = "user_id";
    public static final String ADDRESS_TYPE = "type";
    public static final String ADDRESS_LINE = "address_line";
    public static final String ADDRESS_CITY = "city";

    // CART Table Columns
    public static final String CART_ID = "cart_id";
    public static final String CART_USER_ID = "user_id";
    public static final String CART_PRODUCT_ID = "product_id";
    public static final String CART_QUANTITY = "quantity";

    // Constructor
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Enable Foreign Key support
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Create USERS table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_NAME + " TEXT NOT NULL, "
                + USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + USER_PASSWORD + " TEXT NOT NULL, "
                + USER_PHONE + " TEXT, "
                + USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")");

        // Create CATEGORIES table
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY_NAME + " TEXT NOT NULL UNIQUE, "
                + CATEGORY_ICON + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " ("
                + PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PRODUCT_NAME + " TEXT NOT NULL, "
                + PRODUCT_PRICE + " REAL NOT NULL, "
                + PRODUCT_CATEGORY_ID + " INTEGER, "
                + PRODUCT_DESCRIPTION + " TEXT, "
                + PRODUCT_IMAGE + " TEXT, "
                + PRODUCT_STOCK + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + PRODUCT_CATEGORY_ID + ") REFERENCES "
                + TABLE_CATEGORIES + "(" + CATEGORY_ID + ")"
                + ")");

        // Create ORDERS table
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " ("
                + ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ORDER_USER_ID + " INTEGER NOT NULL, "
                + ORDER_TOTAL + " REAL NOT NULL, "
                + ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + ORDER_STATUS + " TEXT DEFAULT 'pending', "
                + "FOREIGN KEY(" + ORDER_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + USER_ID + ")"
                + ")");

        // Create ORDER ITEMS table
        db.execSQL("CREATE TABLE " + TABLE_ORDER_ITEMS + " ("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_ORDER_ID + " INTEGER NOT NULL, "
                + ITEM_PRODUCT_ID + " INTEGER NOT NULL, "
                + ITEM_QUANTITY + " INTEGER NOT NULL, "
                + ITEM_PRICE + " REAL NOT NULL, "
                + "FOREIGN KEY(" + ITEM_ORDER_ID + ") REFERENCES "
                + TABLE_ORDERS + "(" + ORDER_ID + "), "
                + "FOREIGN KEY(" + ITEM_PRODUCT_ID + ") REFERENCES "
                + TABLE_PRODUCTS + "(" + PRODUCT_ID + ")"
                + ")");

        // Create ADDRESSES table
        db.execSQL("CREATE TABLE " + TABLE_ADDRESSES + " ("
                + ADDRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ADDRESS_USER_ID + " INTEGER NOT NULL, "
                + ADDRESS_TYPE + " TEXT, "
                + ADDRESS_LINE + " TEXT, "
                + ADDRESS_CITY + " TEXT, "
                + "FOREIGN KEY(" + ADDRESS_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + USER_ID + ")"
                + ")");

        // Create CART table
        db.execSQL("CREATE TABLE " + TABLE_CART + " ("
                + CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CART_USER_ID + " INTEGER NOT NULL, "
                + CART_PRODUCT_ID + " INTEGER NOT NULL, "
                + CART_QUANTITY + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + CART_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + USER_ID + "), "
                + "FOREIGN KEY(" + CART_PRODUCT_ID + ") REFERENCES "
                + TABLE_PRODUCTS + "(" + PRODUCT_ID + ")"
                + ")");

        // Insert default data
        insertDefaultCategories(db);
        insertSampleProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDRESSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    // insert: Groceries=1, Household=2, Personal Care=3, Stationery=4
    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] categories = {"Groceries", "Household", "Personal Care", "Stationery"};
        String[] icons = {"🛒", "🏠", "💄", "📝"};

        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put(CATEGORY_NAME, categories[i]);
            values.put(CATEGORY_ICON, icons[i]);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        Object[][] products = {
                // Groceries
                {"Basmati Rice - 1kg", 350.00, 1, "Premium quality basmati rice", "img_rice", 100},
                {"Imorich French Vanilla - 1L", 1290.00, 1, "Rich and creamy French vanilla ice cream", "img_ice_cream", 0},
                {"Munchee Choc Shock - 90g", 300.00, 1, "Delicious chocolate biscuits", "img_chocolate", 20},
                {"Tiara Sponge Layer Cake - 310g", 550.00, 1, "Soft and fluffy sponge cake", "img_cake", 8},

                // Household
                {"Vim Dishwash Liquid Anti Smell 500ml", 450.00, 2, "Anti smell dishwash liquid 500ml", "img_vim_dishwash", 50},
                {"Lysol Lavender Disinfectant 500ml", 500.00, 2, "Lavender disinfectant kills 99.9% germs", "img_lysol", 30},

                // Personal Care
                {"Lux Soap Jasmine And Vitamin E 100g", 170.00, 3, "Jasmine and Vitamin E moisturizing soap", "img_lux_soap", 40},
                {"Sunsilk Onion & Jojoba Oil 200ml", 750.00, 3, "Hair fall resist shampoo", "img_sunsilk", 15},

                // Stationery
                {"Promate Notebook Single A6 80P", 90.00, 4, "Single ruled A6 notebook 80 pages", "img_promate_notebook", 100},
                {"Atlas Pen Chooty II Assorted 3Pkt", 85.00, 4, "Assorted color pen pack of 3", "img_atlas_pen", 75}
        };

        for (Object[] product : products) {
            ContentValues values = new ContentValues();
            values.put(PRODUCT_NAME, (String) product[0]);
            values.put(PRODUCT_PRICE, (Double) product[1]);
            values.put(PRODUCT_CATEGORY_ID, (Integer) product[2]);
            values.put(PRODUCT_DESCRIPTION, (String) product[3]);
            values.put(PRODUCT_IMAGE, (String) product[4]);
            values.put(PRODUCT_STOCK, (Integer) product[5]);
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    // USER OPERATIONS

    public boolean addUser(String name, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password); // hash passwords in production
        values.put(USER_PHONE, phone);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS
                        + " WHERE " + USER_EMAIL + " = ? AND "
                        + USER_PASSWORD + " = ?",
                new String[]{email, password}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + " = ?",
                new String[]{email}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + " = ?",
                new String[]{email}
        );
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;
        Cursor cursor = db.rawQuery(
                "SELECT " + USER_ID + " FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + " = ?",
                new String[]{email}
        );
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(USER_ID);
            if (columnIndex != -1) {
                userId = cursor.getInt(columnIndex);
            }
        }
        cursor.close();
        db.close();
        return userId;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id"},
                "email=?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        return -1;
    }

    // PRODUCT OPERATIONS

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
    }

    public Cursor getProductsByCategoryId(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS
                        + " WHERE " + PRODUCT_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)}
        );
    }

    public Cursor getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)}
        );
    }

    // CART OPERATIONS

    public long addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_CART
                        + " WHERE " + CART_USER_ID + " = ? AND " + CART_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}
        );

        long result;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            int quantityIndex = cursor.getColumnIndex(CART_QUANTITY);
            if (quantityIndex != -1) {
                int newQty = cursor.getInt(quantityIndex) + quantity;
                ContentValues values = new ContentValues();
                values.put(CART_QUANTITY, newQty);
                result = db.update(TABLE_CART, values,
                        CART_USER_ID + "=? AND " + CART_PRODUCT_ID + "=?",
                        new String[]{String.valueOf(userId), String.valueOf(productId)});
            } else {
                result = -1;
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(CART_USER_ID, userId);
            values.put(CART_PRODUCT_ID, productId);
            values.put(CART_QUANTITY, quantity);
            result = db.insert(TABLE_CART, null, values);
        }
        cursor.close();
        db.close();
        return result;
    }

    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, "
                + "p." + PRODUCT_NAME + " as product_name, "
                + "p." + PRODUCT_PRICE + " as product_price, "
                + "p." + PRODUCT_IMAGE + " as product_image "
                + "FROM " + TABLE_CART + " c "
                + "LEFT JOIN " + TABLE_PRODUCTS + " p "
                + "ON c." + CART_PRODUCT_ID + " = p." + PRODUCT_ID + " "
                + "WHERE c." + CART_USER_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public int getCartItemCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + CART_QUANTITY + ") FROM " + TABLE_CART
                        + " WHERE " + CART_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public boolean updateCartQuantity(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (quantity <= 0) {
            int rows = db.delete(TABLE_CART,
                    CART_USER_ID + "=? AND " + CART_PRODUCT_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            db.close();
            return rows > 0;
        } else {
            ContentValues values = new ContentValues();
            values.put(CART_QUANTITY, quantity);
            int rows = db.update(TABLE_CART, values,
                    CART_USER_ID + "=? AND " + CART_PRODUCT_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            db.close();
            return rows > 0;
        }
    }

    public boolean clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CART, CART_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // ORDER OPERATIONS

    public long addOrder(int userId, double totalPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ORDER_USER_ID, userId);
        values.put(ORDER_TOTAL, totalPrice);
        values.put(ORDER_STATUS, "pending");

        long orderId = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return orderId;
    }

    public boolean addOrderItem(int orderId, int productId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_ORDER_ID, orderId);
        values.put(ITEM_PRODUCT_ID, productId);
        values.put(ITEM_QUANTITY, quantity);
        values.put(ITEM_PRICE, price);

        long result = db.insert(TABLE_ORDER_ITEMS, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ORDER_STATUS, status);
        int rows = db.update(TABLE_ORDERS, values, ORDER_ID + "=?",
                new String[]{String.valueOf(orderId)});
        db.close();
        return rows > 0;
    }

    public Cursor getUserOrders(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_ORDERS
                        + " WHERE " + ORDER_USER_ID + " = ?"
                        + " ORDER BY " + ORDER_DATE + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getOrderItems(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.*, p." + PRODUCT_NAME + " as product_name "
                + "FROM " + TABLE_ORDER_ITEMS + " oi "
                + "LEFT JOIN " + TABLE_PRODUCTS + " p "
                + "ON oi." + ITEM_PRODUCT_ID + " = p." + PRODUCT_ID + " "
                + "WHERE oi." + ITEM_ORDER_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    // ADDRESS OPERATIONS

    public long addAddress(int userId, String type, String addressLine, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ADDRESS_USER_ID, userId);
        values.put(ADDRESS_TYPE, type);
        values.put(ADDRESS_LINE, addressLine);
        values.put(ADDRESS_CITY, city);

        long id = db.insert(TABLE_ADDRESSES, null, values);
        db.close();
        return id;
    }

    public Cursor getUserAddresses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_ADDRESSES + " WHERE " + ADDRESS_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean updateAddress(int addressId, String type, String addressLine, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ADDRESS_TYPE, type);
        values.put(ADDRESS_LINE, addressLine);
        values.put(ADDRESS_CITY, city);

        int rows = db.update(TABLE_ADDRESSES, values, ADDRESS_ID + "=?",
                new String[]{String.valueOf(addressId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteAddress(int addressId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_ADDRESSES, ADDRESS_ID + "=?",
                new String[]{String.valueOf(addressId)});
        db.close();
        return rows > 0;
    }
    // CATEGORIES OPERATIONS

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);
    }

    public Cursor getCategoryById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)}
        );
    }
}