# GPInAppBilling
##[应用内购买官方文档](https://developer.android.com/google/play/billing/index.html)
##**注意点**
####1. 请不要在主线程上调用 getSkuDetails 方法。 调用此方法会触发网络请求，进而阻塞主线程。 请创建单独的线程并从该线程内部调用 getSkuDetails 方法。
####2. 应用内商品一经购买，就会被视为“被拥有”且无法从 Google Play 购买。 您必须对应用内商品发送消耗请求，然后 Google Play 才能允许再次购买。（订阅不能被消耗）
####3. 请不要在主线程上调用 consumePurchase 方法。 调用此方法会触发网络请求，进而阻塞主线程。 请创建单独的线程并从该线程内部调用 consumePurchase 方法。
####4. 您必须首先发送消耗请求，才能向用户配置可消耗的应用内购买商品。 确保已从 Google Play 接收到成功的消耗请求，然后再配置商品。
