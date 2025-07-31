<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <title>Optymalizator Cięcia v3 - Baza Materiałów</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background-color: #f4f7f6; color: #333; margin: 0; padding: 20px; }
        .main-container { max-width: 1600px; margin: 0 auto; }
        h1, h2, h3 { color: #2c3e50; padding-bottom: 10px; }
        h1, h2 { border-bottom: 2px solid #3498db; }
        .panel { background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); padding: 20px; margin-bottom: 20px; }
        .grid-dane { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; align-items: end; }
        label { display: block; font-weight: bold; margin-bottom: 5px; }
        input[type="number"], select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; height: 38px; }
        button { background-color: #3498db; color: white; border: none; padding: 10px 15px; font-size: 16px; font-weight: bold; border-radius: 5px; cursor: pointer; transition: background-color 0.3s; height: 38px;}
        button:hover { background-color: #2980b9; }
        button.run-button { background-color: #2ecc71; padding: 12px 25px; height: auto; }
        button.run-button:hover { background-color: #27ae60; }
        #podsumowanie { background-color: #ecf0f1; border-left: 5px solid #3498db; padding: 15px; margin-bottom: 20px; font-size: 1.1em; }
        #podsumowanie p { margin: 5px 0; }
        .canvas-wrapper { width: 100%; overflow-x: auto; background-color: #e8f0fe; padding: 10px; border-radius: 5px; }
        canvas { border: 1px solid #bdc3c7; background-color: white; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; font-weight: bold; }
        tr:nth-child(even) { background-color: #f9f9f9; }
    </style>
</head>
<body>
    <div class="main-container">
        <h1>Optymalizator Cięcia 2D</h1>

        <div class="panel">
            <h2>Dane Główne</h2>
            <div class="grid-dane">
                <div>
                    <label for="szerRolki">Szerokość Rolki (mm):</label>
                    <input type="number" id="szerRolki" value="1600">
                </div>
                <div>
                    <label for="dlRolki">Długość Rolki (mm):</label>
                    <input type="number" id="dlRolki" value="5000">
                </div>
                <div>
                    <label for="skala">Skala wizualizacji:</label>
                    <input type="number" id="skala" value="0.06" step="0.01">
                </div>
            </div>
        </div>

        <div class="panel">
            <h2>Dodaj Element z Bazy Materiałów</h2>
            <div class="grid-dane">
                <div>
                    <label for="wyborMaterialu">Wybierz materiał:</label>
                    <select id="wyborMaterialu"></select>
                </div>
                 <div>
                    <label for="nowyIlosc">Ilość:</label>
                    <input type="number" id="nowyIlosc" value="1">
                </div>
                 <div>
                    <button onclick="dodajElement()">Dodaj do listy</button>
                </div>
            </div>
        </div>

        <div class="panel">
            <h2>Lista Elementów do Optymalizacji</h2>
            <table id="tabelaElementow">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nazwa Materiału</th>
                        <th>Szerokość</th>
                        <th>Wysokość</th>
                        <th>Ilość</th>
                        <th>Obrót?</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
            <button onclick="uruchomOptymalizacje()" class="run-button">Uruchom Optymalizację</button>
        </div>

        <div class="panel">
            <h2>Wynik Optymalizacji</h2>
            <div id="podsumowanie"></div>
            <div class="canvas-wrapper">
                <canvas id="canvasWynik"></canvas>
            </div>
        </div>
    </div>

    <script src="materials.js"></script>

    <script>
        let listaElementowDoCiecia = [];
        let nextId = 1;
        
        // Funkcja, która uruchamia się po załadowaniu strony
        window.onload = function() {
            const select = document.getElementById('wyborMaterialu');
            // Wypełnij listę rozwijaną materiałami z bazy
            // bazaMaterialow jest teraz dostępne z pliku materials.js
            for (const nazwa in bazaMaterialow) {
                const option = document.createElement('option');
                option.value = nazwa;
                option.textContent = nazwa;
                select.appendChild(option);
            }
        };

        function dodajElement() {
            const nazwaMaterialu = document.getElementById('wyborMaterialu').value;
            const ilosc = parseInt(document.getElementById('nowyIlosc').value);
            const szerRolki = parseFloat(document.getElementById('szerRolki').value);

            if (!nazwaMaterialu) {
                alert("Proszę wybrać materiał z listy.");
                return;
            }
            if (isNaN(ilosc) || ilosc <= 0) {
                alert("Proszę podać prawidłową, dodatnią ilość.");
                return;
            }

            const material = bazaMaterialow[nazwaMaterialu];
            // Sprawdzamy, czy materiał powinien być obrócony na podstawie szerokości rolki
            const obrot = material.szer > szerRolki ? 'TAK' : 'NIE';
            
            listaElementowDoCiecia.push({ 
                id: nextId, 
                nazwa: nazwaMaterialu,
                szer: material.szer, 
                wys: material.wys, 
                ilosc: ilosc, 
                obrot: obrot 
            });
            nextId++;
            
            aktualizujTabele();
            
            document.getElementById('nowyIlosc').value = '1';
        }

        function aktualizujTabele() {
            const tbody = document.getElementById('tabelaElementow').getElementsByTagName('tbody')[0];
            tbody.innerHTML = ''; // Wyczyść istniejące wiersze

            listaElementowDoCiecia.forEach(item => {
                const row = tbody.insertRow();
                row.insertCell(0).innerText = item.id;
                row.insertCell(1).innerText = item.nazwa;
                row.insertCell(2).innerText = item.szer;
                row.insertCell(3).innerText = item.wys;
                row.insertCell(4).innerText = item.ilosc;
                row.insertCell(5).innerText = item.obrot;
            });
        }

        function uruchomOptymalizacje() {
            const szerRolki = parseFloat(document.getElementById('szerRolki').value);
            const dlRolki = parseFloat(document.getElementById('dlRolki').value);
            const skala = parseFloat(document.getElementById('skala').value) || 0.05; // Domyślna skala

            if (listaElementowDoCiecia.length === 0) {
                alert("Lista elementów jest pusta. Dodaj przynajmniej jeden element.");
                return;
            }
            
            // --- Tworzenie płaskiej listy elementów na podstawie ilości ---
            // Każdy element z listy "do cięcia" jest powielany o zadaną ilość
            let elementy = [];
            listaElementowDoCiecia.forEach(item => {
                for (let i = 0; i < item.ilosc; i++) {
                    let elSzer = item.szer;
                    let elWys = item.wys;
                    // Jeśli obrót jest 'TAK', zamień szerokość z wysokością
                    if (item.obrot === 'TAK') {
                        [elSzer, elWys] = [elWys, elSzer];
                    }
                    // Dodaj element do listy do optymalizacji z unikalnym ID i obliczonym polem
                    elementy.push({ id: `ID:${item.id}-${i+1}`, nazwa: item.nazwa, szer: elSzer, wys: elWys, pole: elSzer * elWys });
                }
            });

            // Sortuj elementy według powierzchni (od największego do najmniejszego)
            elementy.sort((a, b) => b.pole - a.pole);

            // --- Algorytm układania ---
            let belki = []; // Przechowuje informacje o wszystkich belkach i ułożonych na nich elementach
            const margines = 1; // Margines między elementami
            const przesuniecieMiedzyBelkami = 20; // Odstęp między wizualizacjami belek

            // Dodaj pierwszą belkę
            belki.push({
                szer: szerRolki, dl: dlRolki, leftPos: 10, topPos: 50, // Pozycja na canvasie
                elementy: [], // Elementy ułożone na tej belce
                rzedy: [{ startY: 0, wys: 0, currentX: 0 }] // Bieżące rzędy do układania
            });

            // Iteruj przez każdy element do ułożenia
            for (const e of elementy) {
                let ustawiono = false;
                // Spróbuj umieścić element w istniejących belkach i rzędach
                for (let i = 0; i < belki.length; i++) {
                    const b = belki[i]; // Bieżąca belka
                    for (let j = 0; j < b.rzedy.length; j++) {
                        const rzad = b.rzedy[j]; // Bieżący rząd
                        // Sprawdź, czy element zmieści się w bieżącym rzędzie
                        if (b.szer - rzad.currentX >= e.szer && (e.wys <= rzad.wys || rzad.wys === 0)) {
                            e.posX = rzad.currentX;
                            e.posY = rzad.startY;
                            if (rzad.wys === 0) { rzad.wys = e.wys; } // Ustaw wysokość rzędu, jeśli to pierwszy element
                            rzad.currentX += e.szer + margines; // Przesuń bieżącą pozycję X w rzędzie
                            b.elementy.push(e); // Dodaj element do belki
                            ustawiono = true;
                            break; // Element ułożony, przejdź do następnego elementu
                        }
                    }
                    if (ustawiono) break; // Element ułożony, przejdź do następnego elementu

                    // Jeśli nie zmieścił się w żadnym istniejącym rzędzie, spróbuj utworzyć nowy rząd
                    let sumaWysRzedow = b.rzedy.reduce((acc, r) => acc + r.wys, 0) + (b.rzedy.length > 0 ? (b.rzedy.length) * margines : 0);
                    if (b.dl - sumaWysRzedow >= e.wys) {
                        const nowyRzad = { startY: sumaWysRzedow, wys: e.wys, currentX: e.szer + margines };
                        b.rzedy.push(nowyRzad);
                        e.posX = 0; // Element w nowym rzędzie zaczyna się od X=0
                        e.posY = nowyRzad.startY;
                        b.elementy.push(e);
                        ustawiono = true;
                        break;
                    }
                }
                // Jeśli element nie zmieścił się w żadnej z istniejących belek, utwórz nową belkę
                if (!ustawiono) {
                    const ostatniaBelka = belki[belki.length - 1];
                    // Oblicz pozycję nowej belki na canvasie
                    const leftPos = ostatniaBelka.leftPos + szerRolki * skala + przesuniecieMiedzyBelkami;
                    const nowaBelka = {
                        szer: szerRolki, dl: dlRolki, leftPos: leftPos, topPos: 50,
                        elementy: [], rzedy: []
                    };
                    const nowyRzad = { startY: 0, wys: e.wys, currentX: e.szer + margines };
                    nowaBelka.rzedy.push(nowyRzad);
                    e.posX = 0; e.posY = 0;
                    nowaBelka.elementy.push(e);
                    belki.push(nowaBelka);
                }
            }
            
            // --- Rysowanie wyników na canvasie ---
            const canvas = document.getElementById('canvasWynik');
            const ctx = canvas.getContext('2d');

            // Ustaw rozmiar canvasa na podstawie ostatniej belki
            const ostatniaBelka = belki[belki.length - 1];
            canvas.width = ostatniaBelka.leftPos + szerRolki * skala + 20;
            canvas.height = 50 + dlRolki * skala + 30;
            ctx.clearRect(0, 0, canvas.width, canvas.height); // Wyczyść canvas

            // Pomocnicze funkcje do rysowania
            const rysujProstokat = (x, y, szer, wys, kolorWypelnienia, kolorLinii = 'black') => {
                ctx.fillStyle = kolorWypelnienia;
                ctx.strokeStyle = kolorLinii;
                ctx.fillRect(x, y, szer, wys);
                ctx.strokeRect(x, y, szer, wys);
            };

            const rysujTekst = (tekst, x, y, szer, wys, kolor = 'black', rozmiar = 10) => {
                ctx.fillStyle = kolor; 
                ctx.textAlign = 'center'; 
                ctx.textBaseline = 'middle';
                let aktualnyRozmiar = rozmiar;
                ctx.font = `${aktualnyRozmiar}px sans-serif`;
                // Zmniejsz rozmiar czcionki, jeśli tekst jest za długi
                while (ctx.measureText(tekst.split('\n')[0]).width > szer && aktualnyRozmiar > 6) {
                    aktualnyRozmiar -= 0.5;
                    ctx.font = `${aktualnyRozmiar}px sans-serif`;
                }
                const linie = tekst.split('\n');
                const odstep = aktualnyRozmiar * 1.1; // Odstęp między liniami
                const startY = y + wys / 2 - (linie.length - 1) * odstep / 2; // Pozycja startowa Y dla tekstu
                linie.forEach((linia, i) => ctx.fillText(linia, x + szer / 2, startY + i * odstep));
            };
            
            let sumaOdpadkow = 0;
            let sumaZuzycia = 0;

            belki.forEach((b, index) => {
                // Rysuj obrys belki
                rysujProstokat(b.leftPos, b.topPos, b.szer * skala, b.dl * skala, 'rgba(200, 200, 200, 0.2)');
                rysujTekst(`Belka ${index + 1}`, b.leftPos, b.topPos - 25, b.szer * skala, 20, 'black', 12);

                // Rysuj ułożone elementy
                b.elementy.forEach(e => {
                    rysujProstokat(b.leftPos + e.posX * skala, b.topPos + e.posY * skala, e.szer * skala, e.wys * skala, '#90ee90'); // Zielony kolor
                    rysujTekst(`${e.nazwa}\n${e.szer}x${e.wys} (${e.id})`, b.leftPos + e.posX * skala, b.topPos + e.posY * skala, e.szer * skala, e.wys * skala, 'black', 9);
                    sumaZuzycia += e.pole; // Sumuj powierzchnię użytych elementów
                });
                
                // Rysuj obszary odpadów
                let wysokoscUzyta = 0;
                b.rzedy.forEach(rzad => {
                    const szerUzyta = rzad.currentX - (rzad.currentX > 0 ? margines : 0);
                    const odpSzer = b.szer - szerUzyta; // Odpad na szerokości rzędu
                    if (odpSzer > 1) { // Rysuj tylko, jeśli odpad jest znaczący
                        rysujProstokat(b.leftPos + szerUzyta * skala, b.topPos + rzad.startY * skala, odpSzer * skala, rzad.wys * skala, 'rgba(255, 150, 150, 0.7)'); // Czerwony
                        sumaOdpadkow += odpSzer * rzad.wys;
                    }
                    wysokoscUzyta = rzad.startY + rzad.wys;
                });
                const odpWys = b.dl - wysokoscUzyta; // Odpad na długości belki (na końcu)
                if (odpWys > 1) {
                    rysujProstokat(b.leftPos, b.topPos + wysokoscUzyta * skala, b.szer * skala, odpWys * skala, 'rgba(255, 100, 100, 0.7)'); // Czerwony
                    sumaOdpadkow += odpWys * b.szer;
                }
            });

            // --- Podsumowanie wyników optymalizacji ---
            const podsumowanieDiv = document.getElementById('podsumowanie');
            podsumowanieDiv.innerHTML = `
                <p><strong>Liczba użytych belek:</strong> ${belki.length}</p>
                <p><strong>Powierzchnia elementów:</strong> ${(sumaZuzycia / 1000000).toFixed(3)} m²</p>
                <p><strong>Powierzchnia odpadów:</strong> ${(sumaOdpadkow / 1000000).toFixed(3)} m²</p>
            `;
        }
    </script>
</body>
</html>