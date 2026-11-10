const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const PASSENGER_UID = "passenger_masood_1";
const OTHER_PASSENGER_UID = "passenger_other_2";
const DRIVER_UID = "driver_mirwais_1";
const OTHER_DRIVER_UID = "driver_bilal_2";
const ADMIN_UID = "admin_hq_1";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated caller cannot read users, drivers, or bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(PASSENGER_UID).get());
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("driver_documents").get());
});

test("Passenger can create and read own profile, but cannot read another user's PII profile", async () => {
  const now = new Date();
  const passengerDb = testEnv.authenticatedContext(PASSENGER_UID).firestore();
  await assertSucceeds(
    passengerDb.collection("users").doc(PASSENGER_UID).set({
      userId: PASSENGER_UID,
      fullName: "Masood Khan Achakzai",
      phone: "+923118004421",
      email: "masood@quettaride.pk",
      homeLocation: "Satellite Town, Quetta",
      totalRides: 1,
      rating: 5.0,
      walletBalancePkr: 1500,
      createdAt: now,
      updatedAt: now,
    })
  );

  const otherDb = testEnv.authenticatedContext(OTHER_PASSENGER_UID).firestore();
  await assertFails(otherDb.collection("users").doc(PASSENGER_UID).get());
});

test("Driver cannot self-approve registration (must be PENDING on create)", async () => {
  const now = new Date();
  const driverDb = testEnv.authenticatedContext(DRIVER_UID).firestore();

  // Attempt privilege escalation with APPROVED status -> MUST FAIL
  await assertFails(
    driverDb.collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      fullName: "Mirwais Khan",
      phone: "+923138429910",
      cnic: "54400-8392014-1",
      vehicleCategory: "ALTO",
      vehicleModel: "Suzuki Alto VXR",
      vehiclePlate: "QTA-24-8812",
      approvalStatus: "APPROVED",
      isOnline: true,
      latitude: 30.1798,
      longitude: 66.9750,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid registration with PENDING status -> MUST SUCCEED
  await assertSucceeds(
    driverDb.collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      fullName: "Mirwais Khan",
      phone: "+923138429910",
      cnic: "54400-8392014-1",
      vehicleCategory: "ALTO",
      vehicleModel: "Suzuki Alto VXR",
      vehiclePlate: "QTA-24-8812",
      approvalStatus: "PENDING",
      isOnline: false,
      latitude: 30.1798,
      longitude: 66.9750,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Transactional ride booking lifecycle: passenger creates, first driver accepts, second driver blocked from duplicate acceptance, fare tampering blocked", async () => {
  const now = new Date();
  const passengerDb = testEnv.authenticatedContext(PASSENGER_UID).firestore();
  const driver1Db = testEnv.authenticatedContext(DRIVER_UID).firestore();
  const driver2Db = testEnv.authenticatedContext(OTHER_DRIVER_UID).firestore();

  const bookingRef = passengerDb.collection("bookings").doc("booking_qta_101");
  await assertSucceeds(
    bookingRef.set({
      passengerId: PASSENGER_UID,
      passengerName: "Masood Khan",
      passengerPhone: "+923118004421",
      vehicleCategory: "ALTO",
      pickupName: "Jinnah Road, Quetta",
      pickupLat: 30.1920,
      pickupLng: 67.0099,
      destName: "Satellite Town, Quetta",
      destLat: 30.1620,
      destLng: 67.0220,
      distanceKm: 5.4,
      estimatedMinutes: 14,
      farePkr: 460,
      commissionPkr: 69,
      driverNetPkr: 391,
      paymentMethod: "CASH",
      paymentStatus: "PENDING",
      status: "SEARCHING_DRIVER",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Driver 1 accepts open ride -> MUST SUCCEED
  await assertSucceeds(
    driver1Db.collection("bookings").doc("booking_qta_101").update({
      driverId: DRIVER_UID,
      driverName: "Mirwais Khan",
      vehicleModel: "Suzuki Alto VXR",
      vehiclePlate: "QTA-24-8812",
      status: "DRIVER_ASSIGNED",
      driverLat: 30.1890,
      driverLng: 67.0050,
      updatedAt: new Date(),
    })
  );

  // Driver 2 tries to hijack/accept already assigned ride -> MUST FAIL
  await assertFails(
    driver2Db.collection("bookings").doc("booking_qta_101").update({
      driverId: OTHER_DRIVER_UID,
      driverName: "Bilal Ahmed",
      status: "DRIVER_ASSIGNED",
      updatedAt: new Date(),
    })
  );

  // Driver 1 tries to tamper with farePkr -> MUST FAIL
  await assertFails(
    driver1Db.collection("bookings").doc("booking_qta_101").update({
      farePkr: 9999,
      updatedAt: new Date(),
    })
  );
});
