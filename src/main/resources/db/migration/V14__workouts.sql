-- Gym tracker: exercises, workouts, and the sets logged in them.
-- Weights are integer GRAMS (62500 = 62.5 kg), the same reason money uses
-- integer cents: step maths and equality checks stay exact.

-- An exercise is archived, never deleted, so history never points at a missing row.
CREATE TABLE exercises (
    id                varchar(255) not null,
    name              varchar(255) not null,
    muscle_group      varchar(255) not null,   -- 'chest' | 'back' | 'legs' | 'shoulders' | 'arms' | 'core'
    equipment         varchar(255) not null,   -- 'barbell' | 'dumbbell' | 'machine' | 'cable' | 'bodyweight'
    rep_min           integer not null,        -- bottom of the double-progression rep range
    rep_max           integer not null,        -- top of it: every set here means add weight next time
    weight_step_grams integer not null,        -- how much "add weight" adds; 0 = bodyweight, progress by reps
    archived_at       timestamp,
    created_at        timestamp not null,
    updated_at        timestamp,
    primary key (id)
);

-- performed_on is the local day ('YYYY-MM-DD') the session belongs to, kept
-- beside started_at so a late-night session can't slide across UTC midnight.
CREATE TABLE workouts (
    id           varchar(255) not null,
    performed_on varchar(255) not null,
    started_at   timestamp not null,
    finished_at  timestamp,                    -- null = still in progress (or never tapped Finish)
    notes        text,
    deleted_at   timestamp,
    created_at   timestamp not null,
    updated_at   timestamp,
    primary key (id)
);

CREATE INDEX idx_workouts_performed_on ON workouts(performed_on);

-- One row per logged set. position orders sets within a workout.
CREATE TABLE workout_sets (
    id           varchar(255) not null,
    workout_id   varchar(255) not null references workouts(id),
    exercise_id  varchar(255) not null references exercises(id),
    position     integer not null,
    weight_grams integer not null,             -- per hand for dumbbells
    reps         integer not null,
    warmup       boolean not null default 0,   -- warm-ups never count toward the suggestion
    deleted_at   timestamp,
    created_at   timestamp not null,
    updated_at   timestamp,
    primary key (id)
);

CREATE INDEX idx_workout_sets_workout ON workout_sets(workout_id);
CREATE INDEX idx_workout_sets_exercise ON workout_sets(exercise_id);

-- Starter library. Timestamps are epoch millis, how Hibernate stores Instant here.
-- Steps: barbell 2.5 kg, dumbbell 2 kg, machine 5 kg, cable 2.5 kg, bodyweight 0.
INSERT INTO exercises (id, name, muscle_group, equipment, rep_min, rep_max, weight_step_grams, created_at, updated_at) VALUES
    ('seed-barbell-bench-press',        'Barbell Bench Press',        'chest',     'barbell',    5,  8,  2500, 1791331200000, 1791331200000),
    ('seed-incline-barbell-bench-press','Incline Barbell Bench Press','chest',     'barbell',    6,  10, 2500, 1791331200000, 1791331200000),
    ('seed-dumbbell-bench-press',       'Dumbbell Bench Press',       'chest',     'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-incline-dumbbell-press',     'Incline Dumbbell Press',     'chest',     'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-chest-fly-machine',          'Chest Fly Machine',          'chest',     'machine',    10, 15, 5000, 1791331200000, 1791331200000),
    ('seed-cable-crossover',            'Cable Crossover',            'chest',     'cable',      10, 15, 2500, 1791331200000, 1791331200000),
    ('seed-push-up',                    'Push-up',                    'chest',     'bodyweight', 10, 20, 0,    1791331200000, 1791331200000),
    ('seed-deadlift',                   'Deadlift',                   'back',      'barbell',    3,  6,  2500, 1791331200000, 1791331200000),
    ('seed-barbell-row',                'Barbell Row',                'back',      'barbell',    6,  10, 2500, 1791331200000, 1791331200000),
    ('seed-pull-up',                    'Pull-up',                    'back',      'bodyweight', 5,  12, 0,    1791331200000, 1791331200000),
    ('seed-lat-pulldown',               'Lat Pulldown',               'back',      'cable',      8,  12, 2500, 1791331200000, 1791331200000),
    ('seed-seated-cable-row',           'Seated Cable Row',           'back',      'cable',      8,  12, 2500, 1791331200000, 1791331200000),
    ('seed-dumbbell-row',               'Dumbbell Row',               'back',      'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-back-squat',                 'Back Squat',                 'legs',      'barbell',    5,  8,  2500, 1791331200000, 1791331200000),
    ('seed-front-squat',                'Front Squat',                'legs',      'barbell',    5,  8,  2500, 1791331200000, 1791331200000),
    ('seed-romanian-deadlift',          'Romanian Deadlift',          'legs',      'barbell',    6,  10, 2500, 1791331200000, 1791331200000),
    ('seed-hip-thrust',                 'Hip Thrust',                 'legs',      'barbell',    8,  12, 2500, 1791331200000, 1791331200000),
    ('seed-leg-press',                  'Leg Press',                  'legs',      'machine',    8,  12, 5000, 1791331200000, 1791331200000),
    ('seed-leg-extension',              'Leg Extension',              'legs',      'machine',    10, 15, 5000, 1791331200000, 1791331200000),
    ('seed-leg-curl',                   'Leg Curl',                   'legs',      'machine',    10, 15, 5000, 1791331200000, 1791331200000),
    ('seed-bulgarian-split-squat',      'Bulgarian Split Squat',      'legs',      'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-calf-raise',                 'Calf Raise',                 'legs',      'machine',    10, 15, 5000, 1791331200000, 1791331200000),
    ('seed-overhead-press',             'Overhead Press',             'shoulders', 'barbell',    5,  8,  2500, 1791331200000, 1791331200000),
    ('seed-dumbbell-shoulder-press',    'Dumbbell Shoulder Press',    'shoulders', 'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-lateral-raise',              'Lateral Raise',              'shoulders', 'dumbbell',   10, 15, 2000, 1791331200000, 1791331200000),
    ('seed-face-pull',                  'Face Pull',                  'shoulders', 'cable',      10, 15, 2500, 1791331200000, 1791331200000),
    ('seed-rear-delt-fly',              'Rear Delt Fly',              'shoulders', 'dumbbell',   10, 15, 2000, 1791331200000, 1791331200000),
    ('seed-barbell-curl',               'Barbell Curl',               'arms',      'barbell',    8,  12, 2500, 1791331200000, 1791331200000),
    ('seed-dumbbell-curl',              'Dumbbell Curl',              'arms',      'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-hammer-curl',                'Hammer Curl',                'arms',      'dumbbell',   8,  12, 2000, 1791331200000, 1791331200000),
    ('seed-triceps-pushdown',           'Triceps Pushdown',           'arms',      'cable',      10, 15, 2500, 1791331200000, 1791331200000),
    ('seed-overhead-triceps-extension', 'Overhead Triceps Extension', 'arms',      'cable',      10, 15, 2500, 1791331200000, 1791331200000),
    ('seed-skull-crusher',              'Skull Crusher',              'arms',      'barbell',    8,  12, 2500, 1791331200000, 1791331200000),
    ('seed-dip',                        'Dip',                        'arms',      'bodyweight', 6,  12, 0,    1791331200000, 1791331200000),
    ('seed-hanging-leg-raise',          'Hanging Leg Raise',          'core',      'bodyweight', 8,  15, 0,    1791331200000, 1791331200000),
    ('seed-cable-crunch',               'Cable Crunch',               'core',      'cable',      10, 15, 2500, 1791331200000, 1791331200000),
    ('seed-ab-wheel-rollout',           'Ab Wheel Rollout',           'core',      'bodyweight', 8,  15, 0,    1791331200000, 1791331200000);
