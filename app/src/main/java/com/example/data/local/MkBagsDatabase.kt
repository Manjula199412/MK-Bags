package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItem
import com.example.data.model.CustomerOrder
import com.example.data.model.DealAlert
import com.example.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(
    entities = [Product::class, CartItem::class, CustomerOrder::class, DealAlert::class],
    version = 3,
    exportSchema = false
)
abstract class MkBagsDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun dealAlertDao(): DealAlertDao

    companion object {
        @Volatile
        private var INSTANCE: MkBagsDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN paymentScreenshotKey TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN isLocalCustomerOrder INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE orders ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): MkBagsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MkBagsDatabase::class.java,
                    "mk_bags_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: MkBagsDatabase) {
            val productDao = database.productDao()
            if (productDao.countProducts() == 0) {
                val now = System.currentTimeMillis()
                productDao.insertProducts(
                    listOf(
                        Product(
                            name = "Olive 3-Piece Combo Set",
                            description = "Includes structured daily tote, quilted crossbody sling, and matching zip pouch in sage olive vegan leather.",
                            category = "Combos",
                            priceInr = 1299.0,
                            originalPriceInr = 2299.0,
                            stock = 14,
                            imageKey = "res:img_bag_combo",
                            isCombo = true,
                            isBudgetDeal = false,
                            isLimitedOffer = true,
                            isActive = true,
                            badgeText = "3-PC COMBO",
                            createdAt = now
                        ),
                        Product(
                            name = "Cognac Classic Work Tote",
                            description = "Spacious structured tan tote with reinforced straps, 14-inch laptop sleeve, and brushed gold clasp.",
                            category = "Totes",
                            priceInr = 999.0,
                            originalPriceInr = 1599.0,
                            stock = 18,
                            imageKey = "res:img_bag_tote",
                            isCombo = false,
                            isBudgetDeal = false,
                            isLimitedOffer = true,
                            isActive = true,
                            badgeText = "BESTSELLER",
                            createdAt = now - 1000
                        ),
                        Product(
                            name = "Metro Commuter + Belt Sling Duo",
                            description = "2-in-1 travel & office combo featuring a water-resistant charcoal backpack and camel chest sling.",
                            category = "Combos",
                            priceInr = 1449.0,
                            originalPriceInr = 2499.0,
                            stock = 9,
                            imageKey = "res:img_bag_backpack",
                            isCombo = true,
                            isBudgetDeal = false,
                            isLimitedOffer = true,
                            isActive = true,
                            badgeText = "2-BAG BUNDLE",
                            createdAt = now - 2000
                        ),
                        Product(
                            name = "Daily Essential Mini Tote",
                            description = "Lightweight everyday carryall with smooth zip closure and wide shoulder strap at an unbeatable value price.",
                            category = "Budget Picks",
                            priceInr = 399.0,
                            originalPriceInr = 699.0,
                            stock = 25,
                            imageKey = "res:img_bag_tote",
                            isCombo = false,
                            isBudgetDeal = true,
                            isLimitedOffer = false,
                            isActive = true,
                            badgeText = "UNDER ₹400",
                            createdAt = now - 3000
                        ),
                        Product(
                            name = "Everyday Pocket Crossbody",
                            description = "Compact sling bag for phone, cards, and keys. Ideal for college, errands, and quick evening outings.",
                            category = "Slings",
                            priceInr = 349.0,
                            originalPriceInr = 599.0,
                            stock = 30,
                            imageKey = "res:img_bag_combo",
                            isCombo = false,
                            isBudgetDeal = true,
                            isLimitedOffer = false,
                            isActive = true,
                            badgeText = "BUDGET PICK",
                            createdAt = now - 4000
                        ),
                        Product(
                            name = "Campus Lite Daypack",
                            description = "Sleek padded commuter backpack with dual bottle sleeves and hidden anti-theft back pocket.",
                            category = "Backpacks",
                            priceInr = 749.0,
                            originalPriceInr = 1199.0,
                            stock = 16,
                            imageKey = "res:img_bag_backpack",
                            isCombo = false,
                            isBudgetDeal = false,
                            isLimitedOffer = false,
                            isActive = true,
                            badgeText = "LIGHTWEIGHT",
                            createdAt = now - 5000
                        ),
                        Product(
                            name = "Twin Saver Sling & Pouch Combo",
                            description = "Value-packed 2-piece combo deal with everyday crossbody sling and matching coin/cosmetic pouch.",
                            category = "Combos",
                            priceInr = 549.0,
                            originalPriceInr = 999.0,
                            stock = 12,
                            imageKey = "res:img_bag_combo",
                            isCombo = true,
                            isBudgetDeal = true,
                            isLimitedOffer = true,
                            isActive = true,
                            badgeText = "CHEAP COMBO",
                            createdAt = now - 6000
                        ),
                        Product(
                            name = "Weekend Cabin Carryall",
                            description = "Roomy travel tote built for short getaways with trolley sleeve and soft-touch handles.",
                            category = "Travel",
                            priceInr = 1199.0,
                            originalPriceInr = 1899.0,
                            stock = 7,
                            imageKey = "res:img_bag_tote",
                            isCombo = false,
                            isBudgetDeal = false,
                            isLimitedOffer = false,
                            isActive = true,
                            badgeText = "TRAVEL READY",
                            createdAt = now - 7000
                        )
                    )
                )
            }

            val alertDao = database.dealAlertDao()
            if (alertDao.countAlerts() == 0) {
                alertDao.insertAlert(
                    DealAlert(
                        title = "Weekend Combo Drop Live!",
                        message = "Grab 3-piece bag combos starting at ₹549 and budget totes under ₹400 while stock lasts.",
                        promoCode = "MKCOMBO"
                    )
                )
            }
        }
    }
}
