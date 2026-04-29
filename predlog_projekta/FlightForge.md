# FlightForge — Sistemi bazirani na znanju, predlog projekta

### Momir Stanišić SV39/2022

---

## 1. Motivacija

Avio modelarstvo je hobi koji me interesuje i kojim se bavim već više od jedne decenije. RC (eng. radio controlled) avio modelarstvo je posebno kompleksno jer pored izrade samog aviona takođe uključuje poznavanje komponenti kao što su radio stanice, prijemnici, ESC, BEC, baterije, propeleri, brushless i servo motori. Izbor komponenti je kompleksan zadatak, zahteva obimno znanje o njihovim osobinama ali i načinu na koji komponente zavise međusobno jedna od druge. Čak i za iskusne modelare ovo planiranje je zahtevan proces koji bi se mogao značajno olakšati korišćenjem specijalizovane aplikacije.

## 2. Pregled problema

Prvi izazov kod RC avio modelarstva jeste odabir modela i nalaženje njegovih planova, ali takođe i izbor adekvatne elektronike za taj model. Jedan od najvećih izvora besplatnih planova za RC avio modele jeste FliteTest[^1][^2] i njihova zajednica. Ovi modeli se pretežno baziraju na korišćenju karton pene (eng. foamboard) kao osnovnog gradivnog materijala. Ovo je dodatni izazov jer je izbor karton pene kod nas veoma ograničen. Ona se uglavnom koristi za izradu reklamnih rekvizita, te zbog toga nije uopšte optimizovana za težinu. Pošto se prodaje u velikim pločama, za razliku od elektronskih komponenti, njeno poručivanje iz inostranstva nije isplativo. Jedina realna opcija jeste lokalna ponuda, gde su ploče nekad za istu površinu i debljinu duplo teže od onih za koje su FliteTest planovi originalno kreirani. Ovo dodatno komplikuje planiranje jer da bi se održao balans, model je tada potrebno skalirati. Izazov izbora komponenti nije samo njihova međusobna kompatibilnost, već i činjenica da teži model zahteva snažnije komponente. Snažnije komponente su takođe i teže, povećavajući masu modela i zahtevajući ponovo snažnije komponente… Dakle neophodno je ostvariti adekvatan odnos težine i potiska modela. Isto tako bitan faktor za procenu jeste minimalno trajanje leta pod punim opterećenjem. Naravno, ovo podrazumeva da odabrana kombinacija komponenti može izdržati puno opterećenje. Iako na internetu postoje kalkulatori koji rešavaju jednostavne probleme[^3] i moguće je naći tabele da informacijama o određenim kombinacijama komponenti[^4], ne postoji sistem posebno namenjen avio modelarstvu koji to objedinjuje i automatizuje. Pored pomoći oko planiranja samog modela, ovaj sistem bi takođe pomogao u izboru prikladnog termina za letenje u zavisnosti od kreiranog modela i vremenskih uslova.

## 3. Metodologija rada

### 3.1 Ulazi u sistem

- Odabrani model aviona iz predefinisane baze
- Težina korisnikove karton pene (g/dm²)
- Faktor skaliranja modela (skaliranje dužine i širine, ne površine)
- Minimalni odnos potiska i težine modela
- Minimalno željeno vreme letenja
- Prioritet prilikom traženja kombinacije (min. cena / min. težina / max. vreme leta / max. T/W faktor)
- Korisnički izbor komponenti
- Preferencija metalnih zupčanika kod odabira servo motora (da/ne)
- Težina i potrošnja prijemnika koji korisnik poseduje
- Lokacija korisnika
- Vremenska prognoza (automatski se ažurira)
- Trajanje termina letenja

### 3.2 Izlazi iz sistema

- Težina modela sa i bez komponenti
- Potisak modela — maksimalni potisak koji motor ostvaruje sa odabranim propelerom i baterijom
- T/W faktor — odnos potiska i ukupne težine modela
- Wing loading — odnos ukupne težine modela i površine krila
- Wing cube loading (WCL faktor) — odnos ukupne težine modela i površine krila na 1.5 stepen
- Ocena manevrabilnosti modela — na osnovu odnosa izračunatih i preporučenih T/W i WCL faktora
- Procenjeno minimalno vreme letenja
- Ukupna cena odabrane kombinacije komponenti
- Upozorenja o komponentama koje nisu kompatibilne sa ostatkom i uslovima koji nisu ispunjeni
- Preporučeni termini za letenje sa prikazom temperature i jačine vetra

### 3.3 Baza znanja

Baza znanja obuhvata podatke o modelima aviona i elektronskim komponentama, kao i pravila koja određuju kompatibilnost izabranih komponenti, da li kombinacija odgovara modelu i da li je neki vremenski period prikladan za letenje sa kreiranim modelom. Inicijalna baza komponenti bila bi popunjena proizvodima sa HobbyKing-a[^5]. Korisnici bi takođe imali mogućnost dodavanja novih komponenti, kao i brisanja i izmene postojećih.

#### 3.3.1 Entiteti

**Airplane**
- naziv
- dužina modela
- wingspan — raspon krila
- wing area — površina krila
- wing loading — odnos težine modela i površine krila
- wing cubic loading — kubno opterećenje krila, mera sposobnosti modela da leti sporo
- CG — udaljenost centra gravitacije od nosa modela
- dry weight — težina modela bez elektronskih komponenti
- all-up weight — ukupna težina modela sa predviđenim komponentama
- tip upravljačkih površina (rudder+elevator / rudder+elevator+ailerons / elevons) — određuje broj servo motora
- T/W faktor — predviđeni odnos potiska i težine za taj model

**Motor**
- naziv
- težina
- cena
- dostupnost
- webshop link

**MotorConfiguration**
- motor
- propeler
- broj ćelija baterije — određuje napon napajanja motora (1 ćelija ≈ 3.7V)
- thrust — potisak koji motor ostvaruje sa datim propelerom i naponom, meren u gramima
- maksimalna struja — maksimalna struja koju motor vuče u datoj konfiguraciji, merena u amperima

**Propeller**
- prečnik — dužina propelera
- pitch — korak propelera, rastojanje koje bi propeler prešao u jednom obrtu u idealnim uslovima
- broj krakova — najčešće dva, ponekad tri, retko četiri ili više
- težina
- cena
- dostupnost
- webshop link

**ESC**
- naziv
- kontinuirana struja — maksimalna struja koju ESC može kontinuirano da podnese
- burst struja — maksimalna struja koju ESC može kratkotrajno da podnese
- minimalni broj ćelija baterije
- maksimalni broj ćelija baterije
- BEC izlazni napon — napon koji BEC isporučuje elektronici (servo motorima i prijemniku)
- BEC maksimalna struja — maksimalna struja koju BEC može isporučiti
- težina
- cena
- dostupnost
- webshop link

**Battery**
- naziv
- broj ćelija — određuje napon baterije (1 ćelija = 3.7V)
- kapacitet — kapacitet baterije meren u miliamper-satima (mAh)
- C rating — koeficijent koji određuje maksimalnu izlaznu struju (max. struja = kapacitet baterije × C rating)
- težina
- cena
- dostupnost
- webshop link

**Servo**
- naziv
- torque — moment sile koji servo može da ostvari, meren u kg·cm
- tip zupčanika (plastični/metalni)
- kategorija veličine — numerička ocena prikladnosti servoa za određenu veličinu modela
- struja pri mirovanju
- struja bez opterećenja
- struja pri zaglavljenju — maksimalna struja koju servo vuče kada je blokiran
- težina
- cena
- dostupnost
- webshop link

**Receiver**
- težina
- potrošnja struje

#### 3.3.2 Forward chaining

Forward chaining se primenjuje nakon što je kombinacija komponenti poznata. Na osnovu odabranih komponenti sistem određuje da li su one međusobno kompatibilne, da li je kombinacija zadovoljavajuća za odabrani model, kao i da li u narednim danima postoji prikladan termin za letenje. Pravila su organizovana u tri nivoa:

**Nivo 1** — korekcija težine, računanje ukupne mase i potrošnje modela
- Ako je težina korisnikove karton pene različita od referentne (2.927 g/dm²), onda se dry weight modela koriguje u odnosu na datu težinu
- Ako je faktor skaliranja različit od 1, model se koriguje tako što se dry weight modela množi sa kvadratom faktora skaliranja
- Ako je korigovana težina airframe-a izračunata, onda se uz pomoć `accumulate` funkcije sabira sa težinama svih odabranih komponenti i dobija ukupna težina modela
- Ako su sve komponente odabrane, onda se `accumulate` funkcijom sabira maksimalna potrošnja svih komponenti (motor, servoi, prijemnik) i dobija ukupna maksimalna potrošnja modela

**Nivo 2** — provera ispravnosti kombinacije komponenti
- Ako je ukupna težina modela poznata, onda se računa odnos težine i potiska odabrane motor konfiguracije (T/W faktor)
- Ako je ukupna težina modela poznata, onda se računa WCL faktor kao količnik ukupne težine modela i površine krila prilagođene faktoru skaliranja stepenovan sa 1.5
- Ako je ukupna maksimalna potrošnja modela poznata, onda se računa procenjeno minimalno vreme letenja kao količnik kapaciteta baterije i ukupne maksimalne potrošnje
- Ako je maksimalna izlazna struja baterije (kapacitet × C rating) manja od ukupne maksimalne potrošnje, onda se generiše upozorenje o nekompatibilnoj bateriji
- Ako je maksimalna struja ESC-a manja od maksimalne struje motora, onda se generiše upozorenje o nekompatibilnom ESC-u
- Ako je BEC maksimalna struja manja od ukupne potrošnje servoa i prijemnika, onda se generiše upozorenje o nekompatibilnom BEC-u
- Ako je zadato minimalno vreme letenja i procenjeno vreme letenja je manje od njega, onda se generiše upozorenje o premalom kapacitetu baterije

**Nivo 3** — klasifikacija modela i preporuka termina letenja
- Ako je T/W faktor manji od minimalnog zadatog, onda se generiše upozorenje o nedovoljnom potisku (loš izbor motora, propelera ili baterije)
- Ako su T/W i WCL faktori izračunati i T/W zadovoljava minimum, onda se na osnovu WCL faktora i odnosa izračunatog i preporučenog T/W faktora određuje klasa manevrabilnosti modela
- Ako je klasa manevrabilnosti određena, onda se na osnovu nje i ukupne težine modela određuju pragovi prihvatljivosti brzine vetra — donji prag ispod kojeg je vetar idealan i gornji prag iznad kojeg je vetar neprihvatljiv
- Ako pada kiša, onda se termin označava kao neprikladan
- Ako je noć, onda se termin označava kao neprikladan
- Ako je vetar iznad gornjeg praga, onda se termin označava kao neprikladan
- Ako termin nije neprikladan i nedavno je padala kiša, onda se termin označava kao prihvatljiv
- Ako termin nije neprikladan i vetar je između donjeg i gornjeg praga, onda se termin označava kao prihvatljiv
- Ako termin nije neprikladan i temperatura je između prihvatljive i idealne granice, onda se termin označava kao prihvatljiv
- Ako termin nije neprikladan i termin je blizu zalaska sunca, onda se termin označava kao prihvatljiv
- Ako termin nije neprikladan, nema nedavnih padavina, vetar je ispod donjeg praga, temperatura je u idealnom opsegu i ima dovoljno dnevnog svetla, onda se termin označava kao idealan

#### 3.3.3 Backward chaining

Kada korisnik zatraži automatski predlog kombinacije komponenti, sistem unazad dokazuje da za odabrani model postoji ispravna kombinacija komponenti. Komponente koje su nedostupne sistem ne uzima u obzir pri pretrazi, ali korisnik ih može ručno odabrati. Korisnik može zaključati određene komponente, u tom slučaju sistem te komponente ne bira automatski već njima prilagođava izbor ostalih komponenti.

- Ako je cilj ispravna kombinacija komponenti, onda dokaži da postoji motor konfiguracija (motor/propeler/broj ćelija baterije) sa potiskom koji zadovoljava minimalni T/W faktor za ukupnu težinu i specifikaciju modela
- Ako je motor konfiguracija poznata, onda dokaži da postoji ESC čija maksimalna struja je veća od maksimalne struje motora i čiji opseg broja ćelija je kompatibilan sa odabranom motor konfiguracijom
- Ako je ESC poznat, onda dokaži da postoje servoi odgovarajuće kategorije veličine za dati model, uz poštovanje preferencije tipa zupčanika
- Ako su servoi poznati, onda dokaži da postoji baterija čiji broj ćelija odgovara motor konfiguraciji, čija maksimalna izlazna struja pokriva ukupnu maksimalnu potrošnju modela i ima dovoljan kapacitet ako je zadato minimalno vreme letenja
- Ako nije moguće dokazati postojanje ispravne kombinacije komponenti, onda vrati obrazloženje koji podcilj nije moguće ispuniti

Sistem može generisati različite validne kombinacije u zavisnosti od odabranog prioriteta — minimalna cena, minimalna težina, maksimalno vreme letenja ili maksimalni T/W faktor.

#### 3.3.4 CEP

Sistem periodično prikuplja i analizira vremensku prognozu. Svaki sat u prognozi se ocenjuje kao idealan, prihvatljiv ili neprikladan. Na osnovu zadatog trajanja termina i ocene za svaki sat prognoze, sistem predlaže najbolje termine za letenje. Korisnik može da odabere neki od ponuđenih termina i time označi da tada planira let. Sistem prati odabrane termine i vremensku prognozu, te na osnovu njih reaguje na sledeće događaje:

- Ako je odabrani termin sutra, onda podseti korisnika da se pripremi za let
- Ako se prognoza osvežila i uslovi u odabranom terminu su se pogoršali, onda obavesti korisnika da termin više nije prikladan i predloži nove termine za letenje
- Ako se odabrani termin trenutno odvija i osvežena prognoza pokazuje naglo pogoršanje uslova u narednom periodu, onda upozori korisnika da završi let

## 4. Reference

[^1]: https://forum.flitetest.com/index.php?threads/list-of-70-free-flite-test-plans-from-2012-2020.70627
[^2]: https://forum.flitetest.com/index.php?threads/sp0nz-plans-index.17136
[^3]: https://www.digikey.com/en/resources/conversion-calculators/conversion-calculator-battery-life
[^4]: https://emaxmodel.com/products/emax-multicopter-motor-mt1806
[^5]: https://hobbyking.com