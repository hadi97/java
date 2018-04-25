create database cz_10 --tworzenie

sp_helpdb cz_10 --sprawdzanie plikow i danych baz	
use cz_10 --zmienienie z mast na czwartek10
CREATE TABLE dzialy(
	dzialID int IDENTITY(1,1)
	NOT NULL PRIMARY KEY,
	nazwa varchar(50)
) --tworzenie tabeli

CREATE TABLE pracownicy
(
	pracID  int IDENTITY(1,1) 
	NOT NULL PRIMARY KEY,
	nazwisko varchar(50),
	imie varchar(50),
	wiek int,
	dzialID int 
	FOREIGN KEY REFERENCES 
	dzialy (dzialID)
) 



CREATE TABLE zarobki(
	zarID int IDENTITY(1,1) 
	NOT NULL PRIMARY KEY,
	od datetime,
	brutto money,
	pracID int 
	FOREIGN KEY REFERENCES 
	pracownicy (pracID)
)

--dzialy
insert into dzialy(nazwa) values('Marketing');
insert into dzialy(nazwa) values('Sprzeda?');
insert into dzialy(nazwa) values('Wdro?enia');
insert into dzialy(nazwa) values('Produkcja');

--pracownicy
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Kowalski','Jan',50,1);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Pi?tasa','Janusz',27,1);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Wolnicki','Andrzej',34,1);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Pi?tkowski','Roman',30,1);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Doma?ska','Katarzyna',32,1);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Nowacki','Micha?',null,2);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Krakowski','Mariusz',27,2);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Ziomek','Tomasz',34,3);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Andrzejczak','Jan',20,3);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Jackowska','Maria',null,4);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Nowak','Anna',25,4);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Nowacki','Jan',29,null);
insert into pracownicy(nazwisko,imie,wiek,dzialid) values('Pawe?czyk','Janusz',31,null);


--zarobki
insert into zarobki(od,brutto,pracid) values('01/01/06',12500,1);
insert into zarobki(od,brutto,pracid) values('02/01/06',12550,1);
insert into zarobki(od,brutto,pracid) values('03/01/06',12600,1);
insert into zarobki(od,brutto,pracid) values('01/01/06',2500,2);
insert into zarobki(od,brutto,pracid) values('02/01/06',2550,2);
insert into zarobki(od,brutto,pracid) values('03/01/06',6600,2);
insert into zarobki(od,brutto,pracid) values('04/01/06',6600,2);
insert into zarobki(od,brutto,pracid) values('05/01/06',6250,2);
insert into zarobki(od,brutto,pracid) values('06/01/06',6300,2);
insert into zarobki(od,brutto,pracid) values('01/01/06',2500,3);
insert into zarobki(od,brutto,pracid) values('02/01/06',2550,4);
insert into zarobki(od,brutto,pracid) values('03/01/06',2600,5);
insert into zarobki(od,brutto,pracid) values('01/01/06',2500,6);
insert into zarobki(od,brutto,pracid) values('02/01/06',2550,6);
insert into zarobki(od,brutto,pracid) values('03/01/06',2600,6);
insert into zarobki(od,brutto,pracid) values('01/01/06',2500,7);
insert into zarobki(od,brutto,pracid) values('02/01/06',2550,7);
insert into zarobki(od,brutto,pracid) values('03/01/06',2600,8);
insert into zarobki(od,brutto,pracid) values('01/01/06',2500,9);
insert into zarobki(od,brutto,pracid) values('02/01/06',5550,10);
insert into zarobki(od,brutto,pracid) values('03/01/06',5600,11);

select * from dzialy 
--DRUGIE LAB

create database north_cz_10

select * from klienci

select imie,nazwisko from pracownicy
order by imie asc --order to stronnicowanie asc/desc sortowanie

select * from pracownicy
where lower(nazwisko) like 'j%' --zaczyna sie

select * from pracownicy
where lower(nazwisko) like '%j%' --zawiera j

select * from pracownicy
where lower(nazwisko) like '%j' --konczy sie


--select ;>>
select imie,nazwisko,wiek,
case
	when wiek<25 then 'mlody'
	when wiek<45 then 'sredni'
	when wiek>=45 then 'stary'
else
	'wiek nieznany'
end 'jaki wiek?'
from pracownicy

select imie+' '+nazwisko as dane from pracownicy --konaketnacja
order by dane
--wyszukowanie z kluczami 
select p.imie+' '+p.nazwisko, p.dzialid,d.dzialid,d.nazwa
from pracownicy as p, dzialy as d --aliasy 
where p.dzialid=d.dzialID

--TRZECIE LAB
create database north_eng_cz_10

use north_eng_cz_10

sp_helpdb north_eng_cz_10

--JOIN

select p.imie+' '+p.nazwisko,p.dzialid, d.dzialid,d.nazwa
from pracownicy as p join dzialy as d
on p.dzialid=d.dzialid

--triple join

select p.imie+' '+p.nazwisko,p.dzialid,d.nazwa,zarobki.brutto
from pracownicy as p join dzialy as d
on p.dzialid=d.dzialid
join zarobki on zarobki.pracid=p.pracid

--funkcja agreguj¹ca 

select * from pracownicy
select count(*) from pracownicy --wszystkie pola
select count(wiek) from pracownicy --wartosci tylko bez nulli
select count(nazwisko) from pracownicy --wartosci bez nulli

select sum(wiek)/count(wiek) from pracownicy --srednia bez nulli
select sum(wiek)/count(*) from pracownicy --srednia ze wszystkich nawet z nullami
select sum(wiek) from pracownicy -- cala wartosc

--grupowanie po kolumnach i liczenie sredniej dla dzialu!!
select d.nazwa, avg(zarobki.brutto)
from pracownicy as p join dzialy as d
on p.dzialid=d.dzialid
join zarobki on zarobki.pracid=p.pracid
group by d.nazwa

select max(wiek) as maximum,
min(wiek) as minimum,
avg(wiek) as srednia
from pracownicy
group by dzialid,wiek --wiek psuje, bez wieku lepiej

select nazwa, max(wiek) as maximum,min(wiek) as minimum,avg(wiek) as srednia
from pracownicy
join dzialy on dzialy.dzialid=pracownicy.dzialid
group by nazwa

select nazwa, max(zarobki.brutto) as maximum,min(zarobki.brutto) as minimum,avg(zarobki.brutto) as srednia
from pracownicy
join dzialy on dzialy.dzialid=pracownicy.dzialid
join zarobki on zarobki.pracid=pracownicy.pracid
group by nazwa



select top 1 nazwa, max(zarobki.brutto) as maximum,min(zarobki.brutto) as minimum,avg(zarobki.brutto) as srednia
from pracownicy
join dzialy on dzialy.dzialid=pracownicy.dzialid
join zarobki on zarobki.pracid=pracownicy.pracid
group by nazwa

select nazwisko,imie from pracownicy as p where exists(select * from zarobki as z where z.pracid=p.pracid) 
--Ci ktorzy maja pensje i PRACUJA
select * from dzialy as d where not exists(select * from pracownciy as p where p.dzialid=d.dzialid)
--Ci ktorzy nie maja pensji i teoretycznie pracuja
--in czyli wybor danych rzeczy
select imie,nazwisko,wiek from pracownicy where wiek in(30,31,32,33,34,35)

select imie,nazwisko,wiek from pracownicy where nazwisko in('nowak','kowalski')

--DISTICNT
select imie,nazwisko from pracownicy where pracid in
(
	select distinct pracid from zarobki where brutto > 3000
)

--COINSTRAINT

/* Do tabeli dodaj kolumnê filia, która ma zawieraæ do 20 znaków i na³ó¿ na ni¹ ograniczenie pozwalaj¹ce wpisywaæ 
jedynie miasta Kraków, Warszawa, ³ódŸ oraz Katowice. Nazwij to ograniczenie CK_MIASTO*/

alter table pracownicy add filia
varchar(20);

alter table pracownicy add constraint CK_MIASTO check(filia IN('Krakow','Warszawa','Lodz','Katowice')); --robienie
alter table pracownicy drop constraint CK_MIASTO -- dropowanie

insert into pracownicy(filia) values('jasio')
insert into pracownicy(filia) values('Lodz')

select * from pracownicy

/*Stwórz kolumnê zadanie)1 w tabeli pracownicy, która ma zawieraæ 10 znaków i na³ó¿ na ni¹ ograniczenia pozwalaj¹ce wpisywaæ 
jedynie wyrazy które maj¹ od 2 do 6 lub od  do 10 znaków. Nazwij to ograniczenie CK_ZADANIE1*/

alter table pracownicy add zadanie_1 varchar(10) constraint CK_ZADANIE1 CHECK ((len(zadanie_1) between 2 and 6) 
OR (len(zadanie_1) between 8 and 10))

insert into pracownicy(zadanie_1) values('jasio12')

/* Do tabeli pracownic dodaj kolumne mix ktora ma zawieraæ 6 znaków i 
na³ó¿ na ni¹ ogarniczenie pozwalaj¹ce wpisywaæ jedynie wyrazy wyrazy 
które zawieraj¹ na przemian 3 litery i 3 cyfry. Nazwij to CK_MIX
przyk³ad wartoœci a4f5d9*/

alter table pracownicy add mix varchar(6) constraint CK_MIX CHECK ( mix like '[a-z][0-9][a-z][0-9][a-z][0-9]')
insert into pracownicy(mix) values('aa23zx')

--NIP

alter table pracownicy add nip varchar(10) constraint CK_NIP check(NIP like '[0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9]'))

insert into pracownicy(mix)
values('123-456-78');


--north_cz_10
-- zapytanie zwracajace 2 kolumny:
--nazwa produktu oraz
--cene jedynie produkty drozsze oo 25

select * from Produkty;

select NazwaProduktu as Nazwa, CenaJednostkowa as Cena
from Produkty
where CenaJednostkowa > 25;


-- zapytanie zwracaj¹ce 2 kolumny:
--nazwe produktu
-- oraz nazwe kategorii do
-- ktorej produkt nalezy

select NazwaProduktu, NazwaKategorii
from Produkty as
p join Kategorie as k
on p.IDkategorii=k.IDkategorii;

-- zapytanie zawierajace 3 kolumny:
-- imie, nazwisko pracownika oraz wartosci
-- zamowien przez niego zrealizowanych
-- pod uwage bierzemy
-- jedynie zamowienia z
-- drugiego kwartalu 1997 roku


select * from [Opisy zamówieñ];
select * from Zamówienia;


select Imiê, Nazwisko, Sum
(CenaJednostkowa*iloœæ) as
'wartosc' from Zamówienia as z join
[opisy zamówieñ] as op
on z.IDzamówienia=op.IDzamówienia
join Pracownicy as p
on p.IDpracownika=z.IDpracownika
where DataZamówienia>='1997-04-01'
and DataZamówienia <= '1997-06-30'
group by Imiê, Nazwisko, p.IDpracownika,
z.IDzamówienia;

-- zapytanie zwracaj¹ce 3 kolumny:
-- nazwe kategorii oraz
-- nazwe produktu oraz cene
-- jedynie najdrozsze
-- produkty dla kazdej z kategorii

select NazwaKategorii, NazwaProduktu, CenaJednostkowa from
(select NazwaKategorii, NazwaProduktu,
k.IDkategorii,
CenaJednostkowa from Kategorie as k
join Produkty as p
on k.IDKategorii=p.IDkategorii) as t1
join
(select idkategorii, max(CenaJednostkowa)
as 'maks' from produkty
group by IDkategorii) as t2
on t1.IDkategorii=t2.IDkategorii
where CenaJednostkowa = maks;


-- zapytanie zwracaj¹ce 2 kolumny:
-- kraj oraz ilosc
-- zamowienia z niego realizowane
-- jedynie kraje o
-- najwieszej liczbie zamowien

select Kraj, count(IDzamówienia)
as 'ilosc'
from zamówienia as z
join Klienci as k
on z.IDklienta=k.IDklienta
group by kraj
having count (IDzamówienia) = 
(select max(ilosc) as 'maks' from
(select Kraj, count(IDzamówienia)
as 'ilosc'
from zamówienia as z
join Klienci as k
on z.IDKlienta=k.IDKlienta
group by Kraj) as t1);


--north_eng_cz_10

/*znajdz najtanszy i najdrozszy produkt 
dostarczony przez dostace którego nazwa zaczyna sie na litere a-g i c-p
podaj z jakiej kategorii  on pocodzi oraz w dodatkowej kolumnie 
czy jest to najdrozszy produkt z nazwy kategorii L O L W T F */

SELECT RIGHT('jasio kotek',3) -- z prawej 
SELECT LEFT('jasio kotek',3) -- z lewej 
SELECT CHARINDEX(' ','jasio kotek') -- gdzie znajduje siê spacja 
SELECT SUBSTRING('jasio kotek',4,5)--od ktorego miejsca i ile

select LEFT('jasio kotek',CHARINDEX(' ','jasio kotek')-1) -- spacja - 1 miejsce

like '___%' --niezany znak


SELECT s.CompanyName firma, p.ProductName produkt , p.UnitPrice cena, c.CategoryName
From Suppliers s Join Products p 
On s.SupplierId = p.SupplierId
Inner Join Categories c 
On c.CategoryId=p.CategoryId
Where(lower(Substring(s.CompanyName,1,1)))
Between 'a' And 'g'
And p.UnitPrice In (
Select Max(p.UnitPrice)
From Products p Join Suppliers s 
On s.SupplierId=p.SupplierId)
Union 
SELECT s.CompanyName firma, p.ProductName produkt , p.UnitPrice cena, c.CategoryName
From Suppliers s Join Products p 
On s.SupplierId = p.SupplierId
Inner Join Categories c 
On c.CategoryId=p.CategoryId
Where(lower(Substring(s.CompanyName,1,1)))
Between 'c' And 'p'
And p.UnitPrice In (
Select Min(p.UnitPrice)
From Products p Join Suppliers s 
On s.SupplierId=p.SupplierId)


/*Zapytanie zwaracj¹e tytu³, imiê i nazwisko najm³odszej osoby w ka¿dym z dzia³ów;
przez zdia³ rozumiemy tytu³ pracownika(4)*/

SELECT e.Title, FIrstName,LastName FROM Employees as e JOIN
(SELECT title, MIN(BirthDate) as najmlodszy FROM Employees GROUP BY title) as jasio 
ON e.Title=jasio.Title
Where e.BirthDate=jasio.najmlodszy


--north_cz_10

/*Zapytanie zwaracj¹ce 2 kolumny: nazwe kategorii oraz nazwe firmy
Jedynie firmy ktore dostarczaja najwiecej produktów w danej kategorii */


SELECT NazwaFirmy, NazwaKategorii FROM 
(SELECT k.idkategorii, NazwaFirmy, NazwaKategorii, COUNT(idproduktu) as 'ile' FROM 
Produkty as p JOIN Kategorie as k
ON k.IDkategorii=p.IDkategorii JOIN
Dostawcy as d 
ON d.IDdostawcy=p.IdDostawcy 
GROUP BY k.idkategorii,d.iddostawcy, 
NazwaFirmy, NazwaKategorii) as t2
JOIN
(SELECT idkategorii, MAX(ile) as 'maks' FROM
(SELECT k.idkategorii, d.iddostawcy, COUNT(idproduktu) as 'ile'
FROM Produkty as p JOIN Kategorie as k 
ON k.idkategorii=p.IDkategorii JOIN Dostawcy as d 
ON d.IDdostawcy=p.IDdostawcy
GROUP BY k.idkategorii, d.iddostawcy) as t1
GROUP BY IDkategorii) as t3
On t2.IDkategorii=t3.IDkategorii
WHERE ile=maks


/*Zapytanie zwracaj¹ce 3 kolumny : nazwê firmy, nazwê kategorii 
oraz iloœæ produktów 
dostarczanych w danej kategorii przez dostawcê 
Jedynie dostawy z Niemiec(7)*/

SELECT NazwaFirmy, NazwaKategorii, COUNT(idproduktu) as 'ile' FROM 
Produkty as p JOIN Kategorie as k
ON k.IDkategorii=p.IDkategorii JOIN
Dostawcy as d 
ON d.IDdostawcy=p.IdDostawcy 
where Kraj='Niemcy'
GROUP BY NazwaFirmy,NazwaKategorii,d.IDdostawcy,k.IDkateg

/*Zapytanie zwaracj¹ce 3 kolumny:
Imiê,Nazwisko,iloœæ zamówieñ zrealizowanych po terminie. 
Jedynie osoba z najwiksz¹ iloœci¹ zamówieñ po terminie(1)*/

SELECT Imiê, nazwisko, count(IDzamówienia) as ilosc FROM Pracownicy as p join zamówienia as z 
on p.idpracownika=z.idpracownika 
where DataWysy³ki>DataWymagana
group by imiê, Nazwisko having count(idzamówienia) = (select max(ilosc) 
from (Select Imiê, nazwisko, count(IDzamówienia) as ilosc FROM Pracownicy as p join zamówienia as z 
on p.idpracownika=z.idpracownika where DataWysy³ki > DataWymagana 
group by imiê,nazwisko) as t1)
--MOZLIWOSC FILTROWANIA WIELU WARTOSCI NARAZ L O L O L O L O L O L O L O L O L O L O L O L O L O L O L O L O L O L 


select * from zamówienia


/*Podzaj nazwê najtañszego ze wszystkich produktów(>0) 
stosuj¹c podzapytanie*/

select distinct od.productid, p.productname, od.unitprice
from [order details] od join products p 
on p.productid = od.productid 
where od.unitprice in (select min(unitprice) from [order details]);

/*zapytanie zwaracaj¹ce nazwê firmy, która z³o¿y³a najdro¿sze zamówienie(QUICK-Stop) */

SELECT CompanyName FROM 
"Order Details" as orde JOIN Orders as od
on orde.orderID = od.OrderID JOIN Customers as c on c.CustomerID = od.CustomerID
Group by od.orderID, CompanyName 
HAVING sum(quantity*unitprice) = (select Max(sums) FROM (SELECT sum(quantity*Unitprice) as sums FROM "Order Details"
Group by OrderID) as t)

/*Zapytanie zwracaj¹ce nazwê kategorii 
zawieraj¹cej najwiêcej produktów(Confections)*/


select CategoryName from Categories as c
join Products as p on c.CategoryID=p.CategoryID
Group by CategoryName
having count(ProductID) = (select max(ile) from(select count(ProductID) as ile
from products group by categoryID) as t)

--wy³uskiwanie czasu z systemu 
select convert(VARCHAR(2), DATEPART(HH,GETDATE())) + ':' +
CONVERT(VARCHAR(2), DATEPART(N,GETDATE())) + ':' +
CONVERT(VARCHAR(2), DATEPART(S,GETDATE())) AS [czas]
--funkcje czasowe 
SELECT SYSDATETIME(), --systemowy 
       SYSDATETIMEOFFSET(), --dodaje strefe czasow¹
	   GETDATE(),
	   GETUTCDATE()
	   
	   	   
SELECT DATEADD(dd,-DAY(GETDATE()-1), GETDATE() ) as FirstDayCurrMonth, --pierwszy dzien danego miesiaca
	DATEADD(dd,-DAY(GETDATE() ),GETDATE() ) as LastDayPrevMonth 
--obliczanie wieku 
SELECT FirstName,LastName,BirthDate, 
DATEDIFF(yy,BirthDate,GETDATE()) as Age From dbo.Employees
--wybieranie danego kawa³ka daty lol wtf
SELECT DATEPART(yy,GETDATE()) as CurrentYear,
DATEPART(mm,GETDATE()) as CurrentMonth,
DATEPART(dd,GETDATE()) as CurrentDay,
DATEPART(ww,GETDATE()) as CurrentWeek

SELECT DATENAME(dw,GETDATE()) as DzienTygodnia,
	DATENAME(mm,GETDATE()) as Miesiac


/*szukanie produktów o nazwie zaczynaj¹cej siê na dowoln¹ literê, a druga litera to zaczyna siê 
od c do p i litera nie jest g oraz cena produktu zawiera sie w przedziael 10-100
bez wartosci 90(33)*/

SELECT ProductName, UnitPrice From Products 
where
productname like '___%' and
(SUBSTRING(productname,2,1)>'c' and SUBSTRING
(productname,2,1)<'p')
and (SUBSTRING(productname,2,1)>'C' and SUBSTRING
(productname,2,1)<'P') and 
SUBSTRING(productname,3,1)<>'g' 
and
UnitPrice between '10' and '100' and UnitPrice <>'90'

/*zapytanie zwracaj¹ce 3 loumny: imiê, nazwisko id zamówienia. 
jedynie zamówienia 3 kwarta³u 96
zrealizowanie po terminie(5)*/

SELECT p.imiê, p.nazwisko, z.idzamówienia from pracownicy p join zamówienia as z 
on p.IDpracownika = z.idpracownika 
where DATEPART(month,z.datazamówienia)>6 and datepart(month,z.datazamówienia)<10 and
datepart(year,z.datazamówienia)=1996 and z.datawymagana-z.datawysy³ki<0 











alter table pracownicy add mix constraint sth check (mix like '[a-z][0-9][a-z][0-9][a-z][0-9]');







