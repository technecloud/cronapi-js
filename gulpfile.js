const gulp = require('gulp');
// const autoprefixer = require('gulp-autoprefixer');
const rename = require('gulp-rename');
// const notify = require('gulp-notify');
const minify = require('gulp-babel-minify');
const eslint = require('gulp-eslint');

const fnI18n = function functionI18n() {
  return gulp.src('i18n/').pipe(gulp.dest('i18n/'));
};

const fn = function functionSrc() {
  return gulp.src('cronapi.js')
    .pipe(minify({ mangle: { keepClassName: true } }))
    .pipe(rename('cronapi.min.js'))
    .pipe(gulp.dest('dist/'));
  // .pipe(notify({ message: 'CronApi-JS build finished' }));
};

const fnLint = function functionLint() {
  return gulp.src(['./cronapi.js', '!node_modules/**', '!dist/**', '!bower_components/**'])
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
};

gulp.task('minify', fn);
gulp.task('i18n', fnI18n);
gulp.task('lint', fnLint);
gulp.task('build', ['lint', 'minify', 'i18n']);
gulp.task('default', ['lint', 'build']);
