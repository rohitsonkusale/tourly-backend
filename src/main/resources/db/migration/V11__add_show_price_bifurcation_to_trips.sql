-- Add show_price_bifurcation column to trips table
-- Controls whether travelers can see the fare breakdown (stays, transport, meals)
ALTER TABLE trips ADD COLUMN show_price_bifurcation BOOLEAN NOT NULL DEFAULT TRUE;
