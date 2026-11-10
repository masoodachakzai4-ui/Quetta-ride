import 'package:flutter/material.dart';
import 'dart:math' as math;

void main() {
  runApp(const QuettaRideFlutterApp());
}

// Brand Colors
const Color kNavyDark = Color(0xFF0A192F);
const Color kNavy = Color(0xFF112240);
const Color kYellow = Color(0xFFFACC15);
const Color kGreen = Color(0xFF10B981);
const Color kRed = Color(0xFFEF4444);
const Color kBgLight = Color(0xFFF8FAFC);

enum PlatformMode { website, passenger, driver, admin }

enum VehicleType { bike, rickshaw, alto }

class VehicleConfig {
  final VehicleType type;
  final String name;
  final String subtitle;
  final int capacity;
  int baseFare;
  int perKm;
  final int etaMins;

  VehicleConfig({
    required this.type,
    required this.name,
    required this.subtitle,
    required this.capacity,
    required this.baseFare,
    required this.perKm,
    required this.etaMins,
  });
}

class QuettaLocation {
  final String name;
  final String area;
  final double x;
  final double y;

  const QuettaLocation(this.name, this.area, this.x, this.y);
}

class RideBookingModel {
  final String id;
  final String passengerName;
  final String driverName;
  final VehicleType vehicle;
  final String plate;
  final QuettaLocation pickup;
  final QuettaLocation destination;
  final double distanceKm;
  final int farePkr;
  String status;
  int? rating;

  RideBookingModel({
    required this.id,
    required this.passengerName,
    required this.driverName,
    required this.vehicle,
    required this.plate,
    required this.pickup,
    required this.destination,
    required this.distanceKm,
    required this.farePkr,
    required this.status,
    this.rating,
  });
}

class QuettaRideFlutterApp extends StatelessWidget {
  const QuettaRideFlutterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Quetta Ride — Android & Web',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: kBgLight,
        colorScheme: ColorScheme.fromSeed(
          seedColor: kNavyDark,
          primary: kNavyDark,
          secondary: kYellow,
        ),
      ),
      home: const QuettaMasterShell(),
    );
  }
}

class QuettaMasterShell extends StatefulWidget {
  const QuettaMasterShell({super.key});

  @override
  State<QuettaMasterShell> createState() => _QuettaMasterShellState();
}

class _QuettaMasterShellState extends State<QuettaMasterShell> {
  PlatformMode _mode = PlatformMode.passenger;
  String _selectedCity = 'Quetta';

  final List<QuettaLocation> _locations = const [
    QuettaLocation('Jinnah Road', 'Commercial Center & Kandahari Bazaar', 0.50, 0.48),
    QuettaLocation('Satellite Town', 'Block 3 & Market', 0.65, 0.68),
    QuettaLocation('Sariab Road', 'University of Balochistan Gate', 0.46, 0.78),
    QuettaLocation('Airport Road', 'Alamdar Chowk & Shaikh Zayed', 0.34, 0.28),
    QuettaLocation('Brewery Road', 'Bolan Medical Complex & BUITEMS', 0.24, 0.56),
    QuettaLocation('Quetta Railway Station', 'Station Road Terminal', 0.44, 0.54),
    QuettaLocation('Quetta Airport', 'Samungli International Terminal', 0.20, 0.20),
    QuettaLocation('Kuchlak', 'N-25 Northern Highway Hub', 0.48, 0.12),
    QuettaLocation('Zarghoon Road', 'Civil Secretariat & Serena Chowk', 0.62, 0.40),
  ];

  late List<VehicleConfig> _vehicles;
  late QuettaLocation _pickup;
  late QuettaLocation _destination;
  VehicleType _selectedVehicle = VehicleType.alto;
  bool _driverOnline = true;
  int _driverTodayEarnings = 3850;
  int _driverTodayRides = 9;
  RideBookingModel? _activeRide;
  late List<RideBookingModel> _bookings;

  @override
  void initState() {
    super.initState();
    _initDemoState();
  }

  void _initDemoState() {
    _pickup = _locations[0];
    _destination = _locations[1];
    _vehicles = [
      VehicleConfig(
        type: VehicleType.bike,
        name: 'Motorcycle / Bike',
        subtitle: '1 Passenger • Fast solo commute',
        capacity: 1,
        baseFare: 45,
        perKm: 22,
        etaMins: 3,
      ),
      VehicleConfig(
        type: VehicleType.rickshaw,
        name: 'Auto Rickshaw',
        subtitle: '2–3 Passengers • Traditional local travel',
        capacity: 3,
        baseFare: 80,
        perKm: 36,
        etaMins: 4,
      ),
      VehicleConfig(
        type: VehicleType.alto,
        name: 'Suzuki Alto / Car',
        subtitle: '4 Passengers • Comfortable family ride',
        capacity: 4,
        baseFare: 140,
        perKm: 52,
        etaMins: 5,
      ),
    ];
    _bookings = [
      RideBookingModel(
        id: 'QR-9042',
        passengerName: 'Masood Khan Achakzai',
        driverName: 'Mirwais Khan Achakzai',
        vehicle: VehicleType.alto,
        plate: 'QTA-24-8812',
        pickup: _locations[1],
        destination: _locations[8],
        distanceKm: 5.4,
        farePkr: 420,
        status: 'Completed',
        rating: 5,
      ),
      RideBookingModel(
        id: 'QR-9039',
        passengerName: 'Dr. Ayesha Durrani',
        driverName: 'Bilal Ahmed Mengal',
        vehicle: VehicleType.rickshaw,
        plate: 'QTA-22-4190',
        pickup: _locations[0],
        destination: _locations[4],
        distanceKm: 4.8,
        farePkr: 265,
        status: 'Completed',
        rating: 5,
      ),
    ];
    _activeRide = null;
  }

  double get _currentDistanceKm {
    final dx = (_pickup.x - _destination.x) * 18.5;
    final dy = (_pickup.y - _destination.y) * 18.5;
    final d = math.sqrt(dx * dx + dy * dy);
    return math.max(1.8, (d * 10).roundToDouble() / 10.0);
  }

  int _estimateFare(VehicleConfig cfg) {
    return cfg.baseFare + (_currentDistanceKm * cfg.perKm).round();
  }

  void _confirmBooking() {
    final cfg = _vehicles.firstWhere((v) => v.type == _selectedVehicle);
    final fare = _estimateFare(cfg);
    final booking = RideBookingModel(
      id: 'QR-${9050 + _bookings.length}',
      passengerName: 'Masood Khan Achakzai',
      driverName: _selectedVehicle == VehicleType.alto
          ? 'Mirwais Khan Achakzai'
          : _selectedVehicle == VehicleType.rickshaw
              ? 'Bilal Ahmed Mengal'
              : 'Zafarullah Kakar',
      vehicle: _selectedVehicle,
      plate: _selectedVehicle == VehicleType.alto
          ? 'QTA-24-8812'
          : _selectedVehicle == VehicleType.rickshaw
              ? 'QTA-22-4190'
              : 'QTA-23-1109',
      pickup: _pickup,
      destination: _destination,
      distanceKm: _currentDistanceKm,
      farePkr: fare,
      status: 'Driver Assigned & En Route',
    );
    setState(() {
      _activeRide = booking;
      _bookings.insert(0, booking);
    });
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Booking ${booking.id} Confirmed! Captain ${booking.driverName} is on the way.'),
        backgroundColor: kGreen,
      ),
    );
  }

  void _completeActiveRide() {
    if (_activeRide == null) return;
    setState(() {
      _activeRide!.status = 'Completed';
      _driverTodayEarnings += _activeRide!.farePkr;
      _driverTodayRides += 1;
      _activeRide = null;
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Ride Completed! Earnings & Trip History updated.'),
        backgroundColor: kNavyDark,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: kNavyDark,
        foregroundColor: Colors.white,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: kYellow,
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.location_on, color: kNavyDark, size: 20),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'QUETTA RIDE',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 17),
                ),
                Text(
                  '$_selectedCity • Book Smart. Ride Safe. Reach Anywhere.',
                  style: const TextStyle(fontSize: 11, color: kYellow),
                ),
              ],
            ),
          ],
        ),
        actions: [
          DropdownButtonHideUnderline(
            child: DropdownButton<String>(
              dropdownColor: kNavy,
              value: _selectedCity,
              style: const TextStyle(color: kYellow, fontWeight: FontWeight.bold),
              items: const ['Quetta', 'Karachi', 'Lahore', 'Islamabad', 'Peshawar', 'Gwadar']
                  .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                  .toList(),
              onChanged: (v) {
                if (v != null) setState(() => _selectedCity = v);
              },
            ),
          ),
          IconButton(
            tooltip: 'Reset Demo Data',
            onPressed: () {
              setState(_initDemoState);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Demo data reset to defaults!')),
              );
            },
            icon: const Icon(Icons.restart_alt, color: kYellow),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(52),
          child: Container(
            color: kNavy,
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            child: Row(
              children: [
                _buildModeTab(PlatformMode.website, 'Web Portal', Icons.public),
                const SizedBox(width: 8),
                _buildModeTab(PlatformMode.passenger, 'Passenger App', Icons.Hail),
                const SizedBox(width: 8),
                _buildModeTab(PlatformMode.driver, 'Driver App', Icons.drive_eta),
                const SizedBox(width: 8),
                _buildModeTab(PlatformMode.admin, 'Admin HQ', Icons.admin_panel_settings),
              ],
            ),
          ),
        ),
      ),
      body: switch (_mode) {
        PlatformMode.website => _buildWebsiteView(),
        PlatformMode.passenger => _buildPassengerView(),
        PlatformMode.driver => _buildDriverView(),
        PlatformMode.admin => _buildAdminView(),
      },
    );
  }

  Widget _buildModeTab(PlatformMode mode, String label, IconData icon) {
    final selected = _mode == mode;
    return Expanded(
      child: InkWell(
        onTap: () => setState(() => _mode = mode),
        borderRadius: BorderRadius.circular(10),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          decoration: BoxDecoration(
            color: selected ? kYellow : Colors.white.withOpacity(0.1),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 16, color: selected ? kNavyDark : Colors.white),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  label,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: selected ? kNavyDark : Colors.white,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildWebsiteView() {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Container(
          padding: const EdgeInsets.all(28),
          decoration: BoxDecoration(
            gradient: const LinearGradient(colors: [kNavyDark, kNavy]),
            borderRadius: BorderRadius.circular(24),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(color: kYellow, borderRadius: BorderRadius.circular(30)),
                child: const Text(
                  'QUETTA, BALOCHISTAN • PHASE 1 LAUNCH',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: kNavyDark),
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'Your City. Your Ride. Your Way.',
                style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: Colors.white),
              ),
              const SizedBox(height: 8),
              const Text(
                'Book bikes, rickshaws and comfortable cars across Quetta with just a few taps.',
                style: TextStyle(fontSize: 16, color: Colors.white70),
              ),
              const SizedBox(height: 20),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: kYellow,
                      foregroundColor: kNavyDark,
                      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                    ),
                    onPressed: () => setState(() => _mode = PlatformMode.passenger),
                    icon: const Icon(Icons.directions_car),
                    label: const Text('Book a Ride', style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                  OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: kYellow,
                      side: const BorderSide(color: kYellow, width: 1.5),
                      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                    ),
                    onPressed: () => setState(() => _mode = PlatformMode.driver),
                    icon: const Icon(Icons.badge),
                    label: const Text('Become a Driver', style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 24),
        const Text(
          'Our Multi-Vehicle Fleet for Quetta',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: kNavyDark),
        ),
        const SizedBox(height: 12),
        ..._vehicles.map(
          (v) => Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: kNavyDark,
                child: Icon(
                  v.type == VehicleType.bike
                      ? Icons.two_wheeler
                      : v.type == VehicleType.rickshaw
                          ? Icons.electric_rickshaw
                          : Icons.directions_car,
                  color: kYellow,
                ),
              ),
              title: Text(v.name, style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text('${v.subtitle} • Base PKR ${v.baseFare} + PKR ${v.perKm}/km'),
              trailing: ElevatedButton(
                onPressed: () {
                  setState(() {
                    _selectedVehicle = v.type;
                    _mode = PlatformMode.passenger;
                  });
                },
                child: const Text('Book Now'),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPassengerView() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (_activeRide != null)
          Card(
            color: const Color(0xFFECFDF5),
            margin: const EdgeInsets.only(bottom: 16),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'LIVE RIDE • ${_activeRide!.id} (${_activeRide!.status})',
                    style: const TextStyle(fontWeight: FontWeight.bold, color: kGreen),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${_activeRide!.pickup.name} → ${_activeRide!.destination.name}',
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  Text('Captain: ${_activeRide!.driverName} • Plate: ${_activeRide!.plate} • PKR ${_activeRide!.farePkr}'),
                  const SizedBox(height: 10),
                  ElevatedButton.icon(
                    onPressed: _completeActiveRide,
                    icon: const Icon(Icons.check_circle),
                    label: const Text('Complete Ride & Pay Demo Fare'),
                  ),
                ],
              ),
            ),
          ),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Quetta Route Planner (Simulated GPS Map)',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: kNavyDark),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<QuettaLocation>(
                  value: _pickup,
                  decoration: const InputDecoration(
                    labelText: 'Pickup Location in Quetta',
                    prefixIcon: Icon(Icons.my_location, color: kGreen),
                    border: OutlineInputBorder(),
                  ),
                  items: _locations
                      .map((l) => DropdownMenuItem(value: l, child: Text('${l.name} (${l.area})')))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _pickup = v);
                  },
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<QuettaLocation>(
                  value: _destination,
                  decoration: const InputDecoration(
                    labelText: 'Destination in Quetta',
                    prefixIcon: Icon(Icons.location_on, color: kRed),
                    border: OutlineInputBorder(),
                  ),
                  items: _locations
                      .map((l) => DropdownMenuItem(value: l, child: Text('${l.name} (${l.area})')))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _destination = v);
                  },
                ),
                const SizedBox(height: 12),
                Text(
                  'Estimated Route Distance: $_currentDistanceKm km across $_selectedCity',
                  style: const TextStyle(fontWeight: FontWeight.bold, color: kNavyDark),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        const Text(
          'Select Vehicle Category (Illustrative Prototype Fares)',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: kNavyDark),
        ),
        const SizedBox(height: 8),
        ..._vehicles.map((cfg) {
          final isSelected = cfg.type == _selectedVehicle;
          final fare = _estimateFare(cfg);
          return Card(
            color: isSelected ? const Color(0xFFFEFCE8) : Colors.white,
            shape: RoundedCornerShape(borderRadius: BorderRadius.circular(14)),
            margin: const EdgeInsets.only(bottom: 10),
            child: ListTile(
              onTap: () => setState(() => _selectedVehicle = cfg.type),
              leading: Icon(
                cfg.type == VehicleType.bike
                    ? Icons.two_wheeler
                    : cfg.type == VehicleType.rickshaw
                        ? Icons.electric_rickshaw
                        : Icons.directions_car,
                size: 36,
                color: kNavyDark,
              ),
              title: Text(cfg.name, style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text('${cfg.subtitle}\nETA ~${cfg.etaMins} mins • Capacity: ${cfg.capacity} Pax'),
              trailing: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'PKR $fare',
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: kNavyDark),
                  ),
                  const Text('Demo Estimate', style: TextStyle(fontSize: 10)),
                ],
              ),
            ),
          );
        }),
        const SizedBox(height: 12),
        SizedBox(
          height: 54,
          child: ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: kNavyDark,
              foregroundColor: kYellow,
            ),
            onPressed: _confirmBooking,
            icon: const Icon(Icons.check_circle),
            label: Text(
              'Confirm Booking • PKR ${_estimateFare(_vehicles.firstWhere((v) => v.type == _selectedVehicle))}',
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildDriverView() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          color: kNavyDark,
          child: SwitchListTile(
            value: _driverOnline,
            onChanged: (v) => setState(() => _driverOnline = v),
            title: const Text(
              'Captain Mirwais Khan Achakzai',
              style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
            ),
            subtitle: Text(
              _driverOnline ? 'ONLINE • Receiving Quetta Ride Requests' : 'OFFLINE',
              style: const TextStyle(color: kYellow),
            ),
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      const Text('Today\'s Earnings'),
                      Text(
                        'PKR $_driverTodayEarnings',
                        style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: kGreen),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            Expanded(
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      const Text('Completed Trips'),
                      Text(
                        '$_driverTodayRides Rides',
                        style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: kNavyDark),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildAdminView() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        const Text(
          'Quetta Ride Admin HQ & Fare Management',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: kNavyDark),
        ),
        const SizedBox(height: 12),
        ..._vehicles.map(
          (cfg) => Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(cfg.name, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  Text('Base Fare: PKR ${cfg.baseFare} • Per-KM Rate: PKR ${cfg.perKm}/km'),
                  Slider(
                    value: cfg.perKm.toDouble(),
                    min: 15,
                    max: 100,
                    onChanged: (v) => setState(() => cfg.perKm = v.round()),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}
