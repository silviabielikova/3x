3x game from 2023


Original README from 2023 (submitted as a project for Programming 4 and later as a refactorization project for Information Systems Development):

Témou projektu je hra 3x. Hráč v každom kroku dostane novú položku, ktorú musí umiestniť na plochu. Položky majú rôzne hodnoty, symbolizujú ich rôzne vyrastené rastliny. Ak sa hráčovi podarí položiť vedľa seba aspoň tri rovnaké položky, premenia sa na jednu s vyššou hodnotou. Hráčovi sa pripočítava skóre, každá položka má definované svoje skóre, v prípade spojenia viac ako troch položiek sa skóre navyšuje podľa počtu. Neskôr v hre občas hráč ako položku dostane včelu, tú tiež musí umiestniť a v každom kroku sa následne bude náhodne pohybovať a hráčovi prekážať. Hráč si novú položku (okrem včely) môže namiesto položenia uložiť a využiť ju neskôr, ak dostane ako novú položku včelu, musí ju umiestniť, nemôže využiť uloženú položku.

Trieda Game, ktorá reprezentuje celú logiku hry, má podtriedu Card a enum Items. Ide o jednotlivé karty na ploche a definované položky, ktoré môžu obsahovať. Na začiatku hry sa vytvoria všetky karty, potom sa im položky už iba priraďujú, karta môže byť prázdna. Novú položku je možné uložiť, no iba ak už hráč nemá uloženú inú položku. 

Na začiatku hry sa vygeneruje náhodná hracia plocha. Niektoré rovnaké položky sú už vedľa seba, hráč tak na začiatku dokáže priložením ďalšej položky pomerne jednoducho dostať nejaké body, môže sa potom posunúť ďalej a hra je rýchlejšie zaujímavejšia.

Grafickú stránku hry reprezentuje trieda GUI, obsahuje hraciu plochu a herné štatistiky nad ňou. Tiež sa tam nachádza nová a uložená karta, na ktoré môže hráč klikať, a tak si položky ukladať alebo prepínať medzi uloženou a novou položkou. V hre celý čas hrá hudba, pri klikaní počuť zvuky, pri skórovaní taktiež. 

Keď hra skončí, hráč môže kliknúť na Play again a hrať znovu novú hru, jej začiatok je sprevádzaný znelkou.
