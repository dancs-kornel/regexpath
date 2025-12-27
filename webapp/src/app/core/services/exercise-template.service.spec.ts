import { TestBed } from '@angular/core/testing';
import { ExerciseTemplateService } from './exercise-template.service';
import { DifficultyLevel } from '../models/difficulty.models';
import { ExerciseType } from '../models/assignment.models';

describe('ExerciseTemplateService', () => {
  let service: ExerciseTemplateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ExerciseTemplateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Radio Templates', () => {
    it('should generate EASY radio template with 1 correct and 1 incorrect', () => {
      const template = service.generateRadioTemplate(DifficultyLevel.EASY);
      
      expect(template.type).toBe(ExerciseType.RADIO);
      expect(template.options.length).toBe(2);
      expect(template.options.filter(o => o.correct).length).toBe(1);
      expect(template.options.filter(o => !o.correct).length).toBe(1);
    });

    it('should generate MEDIUM radio template with 1 correct and 2 incorrect', () => {
      const template = service.generateRadioTemplate(DifficultyLevel.MEDIUM);
      
      expect(template.options.length).toBe(3);
      expect(template.options.filter(o => o.correct).length).toBe(1);
      expect(template.options.filter(o => !o.correct).length).toBe(2);
    });

    it('should generate HARD radio template with 1 correct and 3 incorrect', () => {
      const template = service.generateRadioTemplate(DifficultyLevel.HARD);
      
      expect(template.options.length).toBe(4);
      expect(template.options.filter(o => o.correct).length).toBe(1);
      expect(template.options.filter(o => !o.correct).length).toBe(3);
    });

    it('should randomize option order', () => {
      const templates = Array.from({ length: 10 }, () => 
        service.generateRadioTemplate(DifficultyLevel.EASY)
      );
      
      const correctPositions = templates.map(t => 
        t.options.findIndex(o => o.correct)
      );
      
      const uniquePositions = new Set(correctPositions);
      expect(uniquePositions.size).toBeGreaterThan(1);
    });
  });

  describe('Multiple Choice Templates', () => {
    it('should generate EASY template with 2 correct and 1 incorrect', () => {
      const template = service.generateMultipleChoiceTemplate(DifficultyLevel.EASY);
      
      expect(template.type).toBe(ExerciseType.MULTIPLE_CHOICE);
      expect(template.options.length).toBe(3);
      expect(template.options.filter(o => o.correct).length).toBe(2);
      expect(template.options.filter(o => !o.correct).length).toBe(1);
    });

    it('should generate MEDIUM template with 2 correct and 2 incorrect', () => {
      const template = service.generateMultipleChoiceTemplate(DifficultyLevel.MEDIUM);
      
      expect(template.options.length).toBe(4);
      expect(template.options.filter(o => o.correct).length).toBe(2);
      expect(template.options.filter(o => !o.correct).length).toBe(2);
    });

    it('should generate HARD template with 3 correct and 2 incorrect', () => {
      const template = service.generateMultipleChoiceTemplate(DifficultyLevel.HARD);
      
      expect(template.options.length).toBe(5);
      expect(template.options.filter(o => o.correct).length).toBe(3);
      expect(template.options.filter(o => !o.correct).length).toBe(2);
    });
  });

  describe('Regex Templates', () => {
    it('should generate regex template with highlighting enabled by default', () => {
      const template = service.generateRegexTemplate();
      
      expect(template.type).toBe(ExerciseType.REGEX);
      expect(template.solution).toBe('');
      expect(template.enableRealTimeHighlighting).toBe(true);
      expect(template.testCases.positive.length).toBe(1);
      expect(template.testCases.negative.length).toBe(0);
    });

    it('should have empty solution field ready for teacher input', () => {
      const template = service.generateRegexTemplate();
      
      expect(template.solution).toBe('');
      expect(template.testCases.positive[0].text).toBe('');
    });
  });

  describe('Template Common Properties', () => {
    it('should set default points to 1', () => {
      expect(service.generateRadioTemplate(DifficultyLevel.EASY).points).toBe(1);
      expect(service.generateMultipleChoiceTemplate(DifficultyLevel.EASY).points).toBe(1);
      expect(service.generateRegexTemplate().points).toBe(1);
    });

    it('should set default orderIndex to 0', () => {
      expect(service.generateRadioTemplate(DifficultyLevel.EASY).orderIndex).toBe(0);
      expect(service.generateMultipleChoiceTemplate(DifficultyLevel.EASY).orderIndex).toBe(0);
      expect(service.generateRegexTemplate().orderIndex).toBe(0);
    });

    it('should set placeholder question text', () => {
      expect(service.generateRadioTemplate(DifficultyLevel.EASY).question).toBe('Enter your question here');
      expect(service.generateMultipleChoiceTemplate(DifficultyLevel.EASY).question).toBe('Enter your question here');
      expect(service.generateRegexTemplate().question).toBe('Enter your question here');
    });
  });
});