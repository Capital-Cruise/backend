insert into clients (
    created_at,
    updated_at,
    first_name,
    last_name,
    document_type,
    document_number,
    email,
    phone,
    address,
    monthly_income,
    notes
)
values
    (current_timestamp, current_timestamp, 'Mariano', 'Oblitas', 'DNI', '12345678', 'mariano@capitalcruise.local', '999999999', 'Lima', 8500.00, 'Cliente semilla 1'),
    (current_timestamp, current_timestamp, 'Lucia', 'Rojas', 'CE', 'X1234567', 'lucia@capitalcruise.local', '988888888', 'Arequipa', 12000.00, 'Cliente semilla 2'),
    (current_timestamp, current_timestamp, 'Carlos', 'Mendoza', 'PASSPORT', 'P1234567', 'carlos@capitalcruise.local', '977777777', 'Cusco', 15000.00, 'Cliente semilla 3');

insert into vehicles (
    created_at,
    updated_at,
    brand,
    model,
    vehicle_year,
    vehicle_type,
    commercial_price,
    currency,
    description,
    image_url
)
values
    (current_timestamp, current_timestamp, 'Toyota', 'Corolla', 2025, 'SEDAN', 30000.00, 'USD', 'Sedan compacto semilla', 'https://images.capitalcruise.local/toyota-corolla.jpg'),
    (current_timestamp, current_timestamp, 'Suzuki', 'Jimny', 2024, 'SUV', 28000.00, 'USD', 'SUV compacto semilla', 'https://images.capitalcruise.local/suzuki-jimny.jpg'),
    (current_timestamp, current_timestamp, 'Hyundai', 'H-1', 2025, 'VAN', 45000.00, 'PEN', 'Van comercial semilla', 'https://images.capitalcruise.local/hyundai-h1.jpg');
