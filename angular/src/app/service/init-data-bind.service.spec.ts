import { TestBed } from '@angular/core/testing';
import { InitData } from '../model/init-data';
import { InitDataBindService } from './init-data-bind.service';

describe('InitDataBindService', () => {

  let service: InitDataBindService;

  beforeEach(() => {

    TestBed.configureTestingModule({
      providers: [InitDataBindService],
    });

    service = TestBed.get(InitDataBindService);
  });

  it('値の設定と取り出し', () => {

    const expected = require('src/assets/test-data/service/init-data-binder/init-data1.json');
    let actual: InitData;
    service.initData$.subscribe(x => {
      actual = x;
    });

    // 試験実行
    service.initData = expected;

    expect(actual).toEqual(expected);
  });

  it('値の初期化', () => {

    const expected = new InitData();
    let actual: InitData;
    service.initData = require('src/assets/test-data/service/init-data-binder/init-data1.json');
    service.initData$.subscribe(x => {
      actual = x;
    });

    // 試験実行
    service.resetStates();

    expect(actual).toEqual(expected);
  });
});
