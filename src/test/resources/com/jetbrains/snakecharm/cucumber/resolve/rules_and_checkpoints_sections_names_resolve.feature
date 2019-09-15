Feature: Resolve for section names in rules and checkpoints

  Scenario Outline: Resolve for available sections
    Given a snakemake project
    Given I open a file "foo.smk" with text
    """
    rule NAME:
         input: ""
         version: 1
         wrapper: ""
         wildcard_constraints: a=""
         conda: ""
         message: ""
         threads: 1
         benchmark: ""
         shadow: "full"
         output: ""
         group: ""
         singularity: ""
         cwl: ""
         log: ""
         params: a=""
         priority: 1
         resources: a=""
         script: ""
         shell: "{<section>}"
         run:
    """
    When I put the caret after "{
    Then reference in injection should resolve to "<section>" in "foo.smk"
    Examples:
      | section   |
      | input     |
      | version   |
      | threads   |
      | output    |
      | log       |
      | params    |
      | resources |

  Scenario Outline: Resolve section into section if is provided
    Given a snakemake project
    Given I open a file "foo.smk" with text
    """
    <rule_like> NAME:
      <text>
      run:
        <section> #here
    """
    When I put the caret at <section> #here
    Then reference should multi resolve to name, file, times[, class name]
      | <section> | <file> | <times> | <class> |

    Examples:
      | rule_like  | text          | section   | file    | times | class                              |
      | rule       | output: ""    | output    | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | input: ""     | input     | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | params: ""    | params    | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | log: ""       | log       | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | resources: "" | resources | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | version: ""   | version   | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | rule       | threads: 1    | threads   | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | checkpoint | output: ""    | output    | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |
      | checkpoint | threads: 1    | threads   | foo.smk | 1     | SmkRuleOrCheckpointArgsSectionImpl |

  Scenario Outline: Do not resolve to section in another rule, part1 (correct filters in SmkPyReferenceImpl)
    Given a snakemake project
    Given I open a file "foo.smk" with text
     """
     rule foo:
        <text>

     <rule_like> NAME:
       run:
         <section> #here
     """
    When I put the caret at <section> #here
    Then reference should not multi resolve to files
      | foo.smk |
    Examples:
      | rule_like  | text          | section   | target      | file  |
      | rule       | output: ""    | output    | OutputFiles | io.py |
      | rule       | input: ""     | input     | InputFiles  | io.py |
      | rule       | params: ""    | params    | Params      | io.py |
      | rule       | log: ""       | log       | Log         | io.py |
      | rule       | resources: "" | resources | Resources   | io.py |
      | checkpoint | output: ""    | output    | OutputFiles | io.py |

  Scenario Outline: Do not resolve to section in another rule, part2 (correct filters in SmkPyReferenceImpl)
    Given a snakemake project
    Given I open a file "foo.smk" with text
     """
     rule foo:
        <text>

     <rule_like> NAME:
       run:
         <section> #here
     """
    When I put the caret at <section> #here
    Then reference should not resolve
    # At the moment unresolved at all. But we could change it to be resolved into snakemake
    # library methods, but not in foo.smk
    Examples:
      | rule_like  | text        | section |
      | rule       | version: "" | version |

  Scenario Outline: Do not resolve to section in another rule, part3 (correct filters in SmkPyReferenceImpl)
    Given a snakemake project
    Given I open a file "foo.smk" with text
       """
       rule foo:
          <text>

       <rule_like> NAME:
         run:
           <section> #here
       """
    When I put the caret at <section> #here
    Then reference should resolve to "NAME:" in "foo.smk"
    Examples:
      | rule_like  | text       | section |
      | rule       | threads: 1 | threads |
      | checkpoint | threads: 1 | threads |

  Scenario Outline: Resolve section to snakemake library if section undeclared
    Given a snakemake project
    Given I open a file "foo.smk" with text
     """
     rule foo:
        <text>

     <rule_like> NAME:
       run:
         <section> #here
     """
    When I put the caret at <section> #here
    Then reference should resolve to "<target>" in "<file>"
    Examples:
      | rule_like  | text          | section   | target      | file  |
      | rule       | output: ""    | output    | OutputFiles | io.py |
      | rule       | input: ""     | input     | InputFiles  | io.py |
      | rule       | params: ""    | params    | Params      | io.py |
      | rule       | log: ""       | log       | Log         | io.py |
      | rule       | resources: "" | resources | Resources   | io.py |
      | checkpoint | output: ""    | output    | OutputFiles | io.py |
