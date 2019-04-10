import { TestBed } from '@angular/core/testing';

import { ChartConstructionService } from './chart-construction.service';

describe('ChartConstructionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ChartConstructionService = TestBed.get(ChartConstructionService);
    expect(service).toBeTruthy();
  });
});
