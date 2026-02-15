import React from 'react';
import './SeatMap.css';

export default function SeatMap({ allSeats, availableSeatIds, selectedIds, onToggle }) {
  if (!allSeats.length) return <p style={{color:'var(--text-secondary)',textAlign:'center'}}>No seats found for this theater.</p>;

  // Group seats by row
  const rows = allSeats.reduce((acc, seat) => {
    if (!acc[seat.rowLabel]) acc[seat.rowLabel] = [];
    acc[seat.rowLabel].push(seat);
    return acc;
  }, {});

  const getSeatState = (seat) => {
    if (selectedIds.includes(seat.id)) return 'selected';
    if (availableSeatIds.includes(seat.id)) return seat.type === 'VIP' ? 'vip' : 'available';
    return 'taken';
  };

  return (
    <div className="seat-map">
      <div className="seat-map__screen">
        <span>S C R E E N</span>
      </div>

      <div className="seat-map__grid">
        {Object.entries(rows).map(([rowLabel, seats]) => (
          <div key={rowLabel} className="seat-map__row">
            <span className="seat-map__row-label">{rowLabel}</span>
            <div className="seat-map__seats">
              {seats.sort((a, b) => a.seatNumber - b.seatNumber).map(seat => {
                const state = getSeatState(seat);
                const isClickable = state !== 'taken';
                return (
                  <button
                    key={seat.id}
                    className={`seat seat--${state}`}
                    onClick={() => isClickable && onToggle(seat.id)}
                    disabled={!isClickable}
                    title={`${rowLabel}${seat.seatNumber} — ${seat.type}${state === 'taken' ? ' (Taken)' : ''}`}
                  >
                    {seat.seatNumber}
                  </button>
                );
              })}
            </div>
            <span className="seat-map__row-label">{rowLabel}</span>
          </div>
        ))}
      </div>

      <div className="seat-map__legend">
        <div className="legend-item">
          <span className="legend-dot legend-dot--available" />
          <span>Available</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot legend-dot--vip" />
          <span>VIP</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot legend-dot--selected" />
          <span>Selected</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot legend-dot--taken" />
          <span>Taken</span>
        </div>
      </div>
    </div>
  );
}
