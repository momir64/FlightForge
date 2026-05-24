import urllib.request
import os

motors = [
    ("Happymodel SE0802 19000Kv", "https://www.rotorama.com/cms/assets/images/be7ca2f7fce8cc1f706000deb3922051/14239-757.jpg"),
    ("iFlight XING-E 5215 500Kv", "https://www.rotorama.com/cms/assets/docs/0a50a2440620c15e3002e107f756947f/36377-0/xing-e-5215-500kv.jpg"),
    ("Flashhobby A5215 350Kv", "https://www.rotorama.com/cms/assets/docs/e564172d6778612f2a353d9b64192af9/35649-0/b7eb82b1-356b-42d5-b8bd-4cb1e2c77eec.jpg"),
    ("Flashhobby A5215 500Kv", "https://www.rotorama.com/cms/assets/docs/0803142dea279ba6f46b5c5c8ade004f/35648-0/sc82527a86be54f7bb9251524ee286e183.jpg"),
    ("Readytosky 4320 350Kv", "https://www.rotorama.cz/cms/assets/docs/572fea41011e891f365a21e7ce6704fd/27331-0/s719a23f2825a4526b4ef22c2aded3827z.jpg"),
    ("iFlight XING-E 3314 900Kv", "https://www.rotorama.cz/cms/assets/docs/3c63f740ca980ec56df2f9d24779f206/18987-0/d8a58bb7-bdf6-4b01-978c-a6c387f81cb8.jpg"),
    ("BrotherHobby Avenger 2816 810Kv", "https://www.rotorama.cz/cms/assets/docs/ec312ab54c64d0ab9dd5d0a88465c8d0/27025-0/4529963c7f.jpg"),
    ("RCinpower Bison 3220 738Kv", "https://www.rotorama.cz/cms/assets/docs/4c9655162a7e232dea99ae8c38d78d4f/32142-0/bison-3220-738kv-251124-183135.jpg"),
    ("RCinpower EX4214 520Kv", "https://www.rotorama.com/cms/assets/docs/864f8dd3d644f2944c36f547d676c568/32141-0/ex4214-520-251124-183125-1.jpg"),
    ("iFlight XING-E 2809 1250Kv", "https://www.rotorama.com/cms/assets/docs/ffb9ee8b240f58d5d0801d1406ab1d64/16317-0/xing-e-2809-1250kv.jpg"),
    ("BrotherHobby Avenger 2812 V3 1115Kv", "https://www.rotorama.cz/cms/assets/docs/7da24a87aa707026114c7a424e860897/27338-0/9ebb2ed5c8_685581f16a088.jpg"),
    ("AOS Supernova 2207 1980Kv", "https://www.rotorama.com/cms/assets/images/a8a65ac9146be6271070fd771415741d/16398-768.jpg"),
    ("GEPRC EM3115 900Kv", "https://www.rotorama.cz/cms/assets/docs/fed01c80741bd719a3b6817a10836929/18727-0/3-6_65f4a39a723f1.jpg"),
    ("BrotherHobby Avenger 2810 1180Kv", "https://www.rotorama.cz/cms/assets/docs/76c69dce85a2bb989d7677d9498f260d/27225-0/5a9e8243e6.jpg"),
    ("RCinpower GTS V4 2207 2040Kv", "https://www.rotorama.cz/cms/assets/images/014a947c7618bbd9340233150a10f35a/13324-2480.jpg"),
    ("RCinpower Wasp Major V2 22.6-6.6 2100Kv", "https://www.rotorama.cz/cms/assets/docs/fc8884ef8f710631a70ca10e016f9dc8/23815-0/wasp-major-v2-22.6-6.6-2100.jpg"),
    ("RCinpower Wasp Major V2 22.6-6.6 1980Kv", "https://www.rotorama.cz/cms/assets/docs/cd2d38578fdc02d4ac281af6c99ad5ab/23814-0/wasp-major-v2-22.6-6.6-1980.jpg"),
    ("BrotherHobby Avenger 2806.5 1300Kv", "https://www.rotorama.cz/cms/assets/docs/344bf05a9f404b1bb9fd651287c4f383/27336-0/52dec02c22.jpg"),
    ("GEPRC EM2812 900Kv", "https://www.rotorama.cz/cms/assets/docs/5b89d7541ada892d841f1dbe02602e07/18722-0/3-2_65f49e6c59ce9.jpg"),
    ("iFlight XING2 2207 1750Kv", "https://www.rotorama.cz/cms/assets/docs/5856e6e8232ab05b8aa3076adc272ef6/30545-0/xing2-2207-1750kv-data2.png"),
    ("DeepSpace Aether 2207.3 1960KV", "https://www.rotorama.cz/cms/assets/docs/007b4550abc9aa2a3c0dc57915111f21/28751-0/1729912080411-0.jpg"),
    ("BrotherHobby SE 2812 1115KV", "https://www.rotorama.cz/cms/assets/docs/15efa939b7f62b3cdbc21b6497bf02b4/27030-0/756e8271be_684be56bc8f2a.jpg"),
    ("RCinpower GTS V3 1804 3450kv", "https://www.rotorama.cz/cms/assets/docs/61e6cc5192c76e2b4464e148d623757c/23693-0/gts-v3-1804-3450.jpg"),
    ("Emax Eco II 2807 1300Kv", "https://www.rotorama.cz/cms/assets/images/5a974f98c3310f941fff5284aebb65fa/8392-1080.jpg"),
    ("BrotherHobby LA 2005 3450Kv", "https://www.rotorama.cz/cms/assets/docs/43b34ec4a2e796480f9413d525494761/26961-0/1b1f7780b5.jpg"),
    ("BrotherHobby LA 2005 2450Kv", "https://www.rotorama.cz/cms/assets/docs/43b34ec4a2e796480f9413d525494761/26961-0/1b1f7780b5.jpg"),
    ("Axisflying C224 2204.5 1900Kv", "https://www.rotorama.cz/cms/assets/docs/6b0c34d662b254920e962eeda3ae3d3f/23635-0/1711099640654-0-1.jpg"),
    ("iFlight XING 1504 3100Kv", "https://www.rotorama.com/cms/assets/images/d99694a6c72fd7ddca549d2614fec6b9/13802-2774.png"),
    ("RCinpower Bison 22.5-7 1800Kv", "https://www.rotorama.com/cms/assets/images/81470cc20710696555c52a67186abb74/7507-1000.jpg"),
    ("BrotherHobby VY 1507 1550Kv", "https://www.rotorama.cz/cms/assets/docs/c53323aafc3c78347bb1f9191d30b978/28853-0/15071500.jpg"),
    ("Axisflying AE2207 V2 1960Kv", "https://www.rotorama.com/cms/assets/images/d16fed37927ea88edc89422a48cb9995/23545-1175.jpg"),
    ("Emax Eco II 2807 1700Kv", "https://www.rotorama.cz/cms/assets/images/8de71bb9a9dcc260a663337444bfd153/8393-1080.jpg"),
    ("AOS Supernova 1404 4000Kv", "https://www.rotorama.cz/cms/assets/docs/20712f24176ff5d2f8feb5218ff53daa/31959-0/e57211-6f3d189079c542659a4b6517cf0474d0.jpg"),
    ("BetaFPV Lava 1506 4200Kv", "https://cdn.shopify.com/s/files/1/1778/6615/files/9948510e3dcfc96b7b03038a28ab2954_27261fef-16d1-4e5a-835b-a35da2fe4128_600x600.jpg?v=1706581062"),
    ("RCinpower GTS V3 1404.6 Plus 3850Kv", "https://www.rotorama.cz/cms/assets/docs/1d82537b1090a95f6f937695b892de68/23684-0/gts-v3-14046-3850-241014-170341.jpg"),
    ("Flashhobby 2207.5 1900Kv", "https://www.rotorama.cz/cms/assets/docs/3a52bcdd218f3bafef27f571880af243/29043-0/20201224104114-8704_68b5bf33ba062.jpg"),
    ("GEPRC SpeedX2 2207E 1960KV", "https://www.rotorama.com/cms/assets/images/75c7f83577a2e4fc69185d878091a95a/22735-1200.jpg"),
    ("Emax Eco II 2306 1700KV", "https://www.rotorama.cz/cms/assets/images/a31d5baf9b8b8aa36c1486fad5682cbc/8876-1080.jpg"),
    ("OddityRC Spinnybois 2004 2650Kv", "https://www.rotorama.cz/cms/assets/docs/b0f0d486e1a21e12659eec4ee2274782/35014-0/11-9c290891-1588-4455-9796-0cf29c4a086c-1.jpg"),
    ("BrotherHobby VY 1504.5 3950Kv", "https://www.rotorama.cz/cms/assets/docs/935f60b6b12ed55c99d8f1fb71cdd0c3/27013-0/518befd6d3.jpg"),
    ("Sub250 1804 3450Kv", "https://www.rotorama.com/cms/assets/docs/7f622bb2309dd4168a13d7f5dbac22ec/34309-0/6-1d353a84-57d6-4861-8fc2-ba4eab948096.jpg?v=1721893200"),
    ("GEPRC SpeedX2 1505 4300Kv", "https://www.rotorama.cz/cms/assets/docs/8b4013ad3ba52e7b79089592ffacba67/19192-0/4-6.jpg"),
    ("Axisflying C155 4800Kv", "https://www.rotorama.com/cms/assets/docs/d652e7b5c43b77dbe27b5ade15f8c773/32786-0/c155.jpg"),
    ("iFlight XING-E Pro 2207 2450Kv", "https://www.rotorama.cz/cms/assets/docs/1f45b3af23cb75fbe60169ec02fa483c/16574-0/2450kv.jpg"),
    ("RCinpower GTS V3 1304 11500Kv", "https://www.rotorama.com/cms/assets/docs/75771c5e42c328644bf7c9350666758b/32094-0/s50dd565dd7504582a4c3b828ad64813fz.jpg"),
    ("GEPRC SpeedX2 1404 3850Kv", "https://www.rotorama.cz/cms/assets/docs/e1f254fbab5f780fe7469d8afb86537a/26543-0/4-4_6838176f7d17f.jpg"),
    ("FlyfishRC Flash 1303.5 5500kv", "https://www.rotorama.cz/cms/assets/docs/c8b901942f01db30c1962a360f2d7468/21113-0/flash1303.5motor-1.jpg"),
    ("RCinpower GTS V2 1207 6000Kv", "https://www.rotorama.com/cms/assets/images/134f7453ab47acdf1225643ebaedb795/16430-600.png"),
    ("DeepSpace Aether 1505 4000Kv", "https://www.rotorama.cz/cms/assets/docs/2c71d9d15bebf2acbe1c01f18bba3d85/28942-0/1744372632924-0_68aeee7d1077b.jpg"),
    ("Emax Eco II 2004 3000KV", "https://www.rotorama.com/cms/assets/images/2b6b782e090444bc1fda5cc717956dea/10786-1060.jpg"),
    ("Emax Eco II 2004 2400KV", "https://www.rotorama.com/cms/assets/images/2b6b782e090444bc1fda5cc717956dea/10786-1060.jpg"),
    ("BetaFPV LAVA 1104 7200Kv", "https://www.rotorama.cz/cms/assets/docs/53c988451a072d12d3881aff4c0257a3/30219-0/9948510e3dcfc96b7b03038a28ab2954.jpg"),
    ("RCinpower GTS V3 1003 18000Kv", "https://www.rotorama.com/cms/assets/images/3c1fb54e7a3b9c4f7fb089f4b775103c/15056-850.jpg"),
    ("DarwinFPV Bling 1103 8000Kv", "https://www.rotorama.com/cms/assets/docs/41070634de64421bdcd77b5f1baa6f6b/32774-0/the-more-details-for-1103-motor-1.jpg"),
    ("GEPRC SpeedX2 1002 25000Kv", "https://www.rotorama.cz/cms/assets/docs/c12d1be8945b53430d7f1ab8b28df96a/23484-0/4-5_677bd2732ad62.jpg"),
    ("GEPRC SpeedX2 1202.5 10000KV", "https://www.rotorama.cz/cms/assets/docs/7cf0eff70ba2f594c06a52c1d0ffe405/31154-0/4-1-6908d1d5c537f.jpg"),
    ("Flashhobby A1408 3650Kv (5mm)", "https://www.rotorama.cz/cms/assets/docs/02127863faa61a4cfdcd7063d52e4226/29171-0/1408.jpg"),
    ("Flashhobby 2004 V2 2100Kv", "https://www.rotorama.cz/cms/assets/docs/5ce7982d5a40b5fa266920ade3d2d5fd/29110-0/k2004-v2-1750kv-00-00-1-1-810570.jpg"),
    ("Happymodel EX1404 3500Kv", "https://www.rotorama.com/cms/assets/images/3b9fb5d3df2ab5f7249c6b816696a76f/11909-1000.jpg"),
    ("Flashhobby K1303 8000KV", "https://www.rotorama.cz/cms/assets/docs/fdf9588ed9be0486c40cdcba8291ee53/29125-0/1303-motor-drawing-912679_68b958e6ee3fc.jpg"),
    ("GEPRC SpeedX2 1104 7500KV", "https://www.rotorama.com/cms/assets/docs/e5a9675d3217dfb5d3f3353cc9bc39a7/33731-0/6-4_696e37da70985.jpg"),
    ("Flashhobby A1207 5200KV", "https://www.rotorama.cz/cms/assets/docs/55541783f59584e55a348e3573de4bb3/29284-0/snimek-obrazovky-2025-09-08-150134_68bee1a827234.jpg"),
    ("Happymodel RS0802 20000Kv", "https://www.rotorama.com/cms/assets/images/7886e6074ab2ae947f63dbce5c5a4793/11929-665.jpg"),
    ("Happymodel EX0802 19000Kv", "https://www.rotorama.com/cms/assets/images/13e3f388ed502cb8843152ee62c3ee90/11893-833.jpg"),
]

os.makedirs("motors", exist_ok=True)

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Referer": "https://www.rotorama.com/",
}

ok, fail = 0, []
for name, url in motors:
    ext = ".png" if url.split("?")[0].endswith(".png") else ".jpg"
    filename = os.path.join("motors", name + ext)
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as resp:
            with open(filename, "wb") as f:
                f.write(resp.read())
        print(f"OK: {filename}")
        ok += 1
    except Exception as e:
        print(f"FAIL: {name} — {e}")
        fail.append(name)

print(f"\nDone: {ok} downloaded, {len(fail)} failed")
if fail:
    print("Failed:")
    for name in fail:
        print(f"  {name}")