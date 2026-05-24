INSERT INTO airplane_specs (id, name, length, wingspan, wing_area, wing_loading, wing_cubic_loading, cg, dry_weight, all_up_weight, control_surface_type, recommended_tw_factor) VALUES
(1,  'FT Baby Blender',            635.0,   610.0,  24.0,  24.5,  5.0,  82.5,   396.0,   587.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(2,  'FT Bloody Baron',            610.0,   737.0,  15.7,  32.5,  8.2,  58.5,   303.0,   510.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(3,  'FT Bronco',                  927.0,  1086.0,  20.8,  40.0,  8.7,  51.0,   640.0,   830.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack B, C
(4,  'FT Bushwacker',              851.0,  1143.0,  21.9,  32.4,  6.9,  44.0,   521.0,   711.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(5,  'FT Corsair',                 781.0,  1168.0,  22.9,  44.6,  9.3,  50.8,   737.0,  1020.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(6,  'FT Cruiser',                 800.0,  1264.0,  24.1,  39.9,  8.1,  74.0,   766.0,   960.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack C
(7,  'FT Dart',                    305.0,   622.0,  10.8,  25.7,  7.8, 108.0,   181.0,   278.0,  'ELEVONS',                  NULL), -- (twin) Power Pack H
(8,  'FT Edge',                   1016.0,  1016.0,  21.9,  38.0,  8.1,  83.0,   680.0,   835.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(9,  'FT Explorer',                927.0,  1448.0,  29.7,  23.0,  4.2,  57.0,   493.0,   683.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(10, 'FT Flyer',                   660.0,   660.0,  15.9,  15.0,  3.7, 140.0,   238.0,   326.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack B
(11, 'FT Goblin',                  483.0,   762.0,  13.7,  33.7,  9.1,  19.0,   249.0,   463.0,  'ELEVONS',                  NULL), -- (remove this one)
(12, 'FT Guinea Pig',             1105.0,  1473.0,  33.7,  40.0,  6.9,  50.0,   998.0,  1350.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack C
(13, 'FT Kraken',                  838.2,  1816.0,  97.3,  17.9,  1.8, 356.0,  1542.2,  1740.7,  'ELEVONS',                  NULL), -- (twin) Power Pack C
(14, 'FT Legacy',                 1003.0,  1435.0,  32.1,  46.0,  8.1,  64.0,  1111.0,  1475.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack B, C
(15, 'FT LongEZ',                  406.0,   483.0,   6.4,  35.0, 13.9,  NULL,   156.0,   226.0,  'ELEVONS',                  NULL), -- Power Pack A
(16, 'FT MiG-3',                   762.0,  1067.0,  18.0,  38.0,  8.9,  50.8,   453.6,   683.6,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(17, 'FT Mini Alpha',              698.5,   609.0,   9.8,  30.1,  9.6, 355.0,   230.0,   295.0,  'ELEVONS',                  NULL), -- Power Pack F
(18, 'FT Mini Arrow',              439.0,   775.0,  16.6,  14.8,  3.6,  44.5,   218.0,   245.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack A, F
(19, 'FT Mini Bravo',              692.2,   609.0,   9.3,  31.7, 10.5, 355.0,   230.0,   295.0,  'ELEVONS',                  NULL), -- Power Pack F
(20, 'FT Mini Charlie',            705.0,   711.0,  10.1,  29.2,  9.2, 355.0,   230.0,   295.0,  'ELEVONS',                  NULL), -- Power Pack F
(21, 'FT Mini Commuter',           584.0,   762.0,   9.2,  31.0, 10.2,  25.4,   234.0,   284.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack A
(22, 'FT Mini Corsair',            482.0,   610.0,   6.9,  32.0, 12.2,  38.0,   156.0,   222.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack A, F
(23, 'FT Mini DR1',                495.0,   618.2,  21.3,  16.0,  3.5,  46.5,   263.0,   340.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack A, F
(24, 'FT Mini F-22',               667.0,   508.0,  14.2,  14.9,  3.9, 305.0,   134.0,   211.0,  'ELEVONS',                  NULL), -- Power Pack A, F
(25, 'FT Mini Guinea',             660.0,   889.0,  12.1,  29.3,  8.4,  32.0,   255.0,   354.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack A, F
(26, 'FT Mini Mustang',            482.0,   622.0,   7.4,  29.9, 10.9,  25.0,   156.0,   222.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack A, F
(27, 'FT Mini Pietenpol',          533.4,   723.9,  12.2,  22.7,  6.5,  63.5,   200.0,   277.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack F
(28, 'FT Mini Pun Jet',            368.0,   363.0,   6.6,  29.2, 11.4, 184.0,   116.0,   193.0,  'ELEVONS',                  NULL), -- Power Pack A
(29, 'FT Mini Scout',              457.2,   609.0,   8.5,  19.4,  6.6,  44.0,   115.0,   165.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack A
(30, 'FT Mini SE5',                476.0,   609.6,  17.0,  19.2,  4.6,  50.8,   249.5,   326.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack A, F
(31, 'FT Sparrow',                 406.0,   723.0,  11.6,  22.0,  6.4,  44.5,   187.0,   256.0,  'ELEVONS',                  NULL), -- Power Pack A
(32, 'FT Mini Sportster',          432.0,   584.0,   8.8,  30.6, 10.3,  44.0,   204.0,   270.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack F
(33, 'FT Mini Super Bee',          495.0,   635.0,  11.7,  41.2, 12.0,  51.0,   340.0,   482.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack A, F
(34, 'FT Mini Vector',             704.0,   635.0,  16.3,  17.0,  4.4, 355.0,   211.0,   288.0,  'ELEVONS',                  NULL), -- Power Pack A, F
(35, 'FT Spitfire',                978.0,  1220.0,  28.0,  29.0,  5.4,  50.0,   590.0,   816.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(36, 'FT Mustang',                 787.0,  1016.0,  19.5,  27.9,  6.3,  70.0,   453.0,   543.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(37, 'FT Nutball',                 508.0,   490.5,  NULL,  NULL,  3.4, 127.0,   212.0,   280.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack A
(38, 'FT Old Fogey',               838.0,   959.0,  24.3,  20.1,  4.1,  76.0,   377.0,   489.0,  'RUDDER_ELEVATOR',          NULL), -- Power Pack B
(39, 'FT Otter',                   813.0,  1016.0,  19.5,  31.4,  7.1,  64.0,   428.0,   612.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B, C
(40, 'FT P-38',                   1028.0,  1460.0,  25.2,  54.0, 10.8,  45.0,  1020.0,  1360.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack C
(41, 'FT P-40',                    813.0,  1054.0,  19.1,  29.2,  6.7,  50.8,   468.0,   558.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(42, 'FT P-47',                   1016.0,  1206.0,  26.9,  37.2,  7.2,  45.0,   720.0,  1000.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(43, 'FT P-51 Mustang',           1042.0,  1225.0,  26.0,  29.0,  5.7,  76.0,   520.0,   744.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(44, 'FT Racer',                   890.0,  1016.0,  13.4,  50.7, 13.9,  60.0,   500.0,   680.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(45, 'FT Sea Angel',               787.0,  1067.0,  21.0,  41.2,  9.0,  50.8,   635.0,   865.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(46, 'FT Sea Duck',               1067.0,  1397.0,  31.9,  40.8,  7.2,  64.0,  1111.0,  1301.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- (twin) Power Pack B, C
(47, 'FT Simple Cub',              775.0,   965.0,  19.5,  25.1,  5.7,  45.0,   408.0,   489.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B
(48, 'FT Simple Scout',            736.6,   952.5,  20.9,  25.6,  5.6,  62.0,   425.2,   535.8,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack B
(49, 'FT Simple Storch',           990.0,  1460.0,  34.0,  29.6,  5.1,  51.0,   816.0,  1005.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(50, 'FT Spear',                   521.0,  1041.0,  28.1,  30.2,  5.7,  76.0,   658.0,   848.0,  'ELEVONS',                  NULL), -- Power Pack C
(51, 'FT Spitfire (Swappable)',    810.0,  1080.0,  20.6,  25.0,  5.4,  68.5,   420.0,   510.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(52, 'FT Sportster',               762.0,   990.0,  24.5,  25.3,  5.1,  63.0,   430.0,   620.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack C
(53, 'FT Tiny Trainer',            648.0,   940.0,  12.5,  22.0,  6.1,  44.0,   193.0,   270.0,  'RUDDER_ELEVATOR_AILERONS', NULL), -- Power Pack A, F
(54, 'FT Twin Sparrow',            406.0,   723.0,  11.6,  22.0,  6.5,  69.0,   158.0,   258.0,  'RUDDER_ELEVATOR',          NULL), -- (twin) Power Pack H
(55, 'FT Versa Wing',              483.0,   965.0,  27.9,  17.9,  3.4,  NULL,   300.0,   500.0,  'ELEVONS',                  NULL), -- Power Pack B, C
(56, 'FT X-29',                   1155.7,   698.5,  15.35, 65.2, 16.6, 577.8,   748.4,   998.4,  'RUDDER_ELEVATOR_AILERONS', NULL); -- Power Pack C


-- Power pack A - 275g
-- Power pack B - 750g
-- Power pack C - 1000g
-- Power pack F - 400g
-- Power pack H - 96g
