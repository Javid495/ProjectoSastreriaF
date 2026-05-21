        function cambiarFiltroEstado(estadoSeleccionado, botonElemento) {
            // 1. Quitar la clase active de todas las pestañas
            const pestañas = document.querySelectorAll('.tab-estado');
            pestañas.forEach(tab => tab.classList.remove('active'));
            
            // 2. Marcar la pestaña clickeada como activa
            botonElemento.classList.add('active');
            
            // Aquí puedes meter tu fetch o la lógica de JS para refrescar 
            // el contenedor "#tablero-pedidos" con los registros de la DB de Java.
            console.log("Filtrando el tablero de confección por: " + estadoSeleccionado);
        }